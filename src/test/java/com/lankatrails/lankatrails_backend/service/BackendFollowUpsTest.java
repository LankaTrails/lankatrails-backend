package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.UserPreferencesDTO;
import com.lankatrails.lankatrails_backend.dtos.request.UserPreferencesUpdateRequest;
import com.lankatrails.lankatrails_backend.dtos.request.LoginRequest;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ComplaintInfoResponse;
import com.lankatrails.lankatrails_backend.dtos.response.ProviderInfoResponse;
import com.lankatrails.lankatrails_backend.exception.BadCredentialsException;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.UserPreferences;
import com.lankatrails.lankatrails_backend.model.enums.ApprovalStatus;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.ComplaintStatus;
import com.lankatrails.lankatrails_backend.repositories.ComplaintRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.security.service.LoginRateLimiter;
import com.lankatrails.lankatrails_backend.service.impl.AuthServiceImpl;
import com.lankatrails.lankatrails_backend.service.impl.ComplaintServiceImpl;
import com.lankatrails.lankatrails_backend.service.impl.UserServiceimpl;
import org.modelmapper.ModelMapper;
import org.mockito.Spy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackendFollowUpsTest {

    @Nested
    class LoginThrottling {
        @SuppressWarnings("unchecked")
        private final RedisTemplate<String, String> redis = mock(RedisTemplate.class);
        @SuppressWarnings("unchecked")
        private final ValueOperations<String, String> ops = mock(ValueOperations.class);
        private final Map<String, Long> counters = new HashMap<>();
        // 3 attempts per IP + account, 5 per IP
        private final LoginRateLimiter limiter = new LoginRateLimiter(redis, 3, 5, 60);

        @BeforeEach
        void fakeRedis() {
            when(redis.opsForValue()).thenReturn(ops);
            lenient().when(ops.increment(anyString())).thenAnswer(inv -> counters.merge(inv.getArgument(0), 1L, Long::sum));
        }

        @Test
        void differentAccountsFromOneMachineHaveSeparateBuckets() {
            for (int i = 0; i < 3; i++) {
                assertThat(limiter.tryAcquire("172.19.0.1", "admin@test.dev")).isTrue();
            }
            assertThat(limiter.tryAcquire("172.19.0.1", "admin@test.dev")).isFalse();
            // the admin hitting its limit does not block the provider or tourist
            assertThat(limiter.tryAcquire("172.19.0.1", "provider@test.dev")).isTrue();
        }

        @Test
        void perIpLimitCapsAccountSpraying() {
            for (int i = 0; i < 5; i++) {
                assertThat(limiter.tryAcquire("10.0.0.9", "user" + i + "@test.dev")).isTrue();
            }
            assertThat(limiter.tryAcquire("10.0.0.9", "another@test.dev")).isFalse();
            assertThat(limiter.tryAcquire("10.0.0.10", "another@test.dev")).isTrue();
        }

        @Test
        void accountKeyIgnoresEmailCaseAndSetsTheWindowOnce() {
            limiter.tryAcquire("10.0.0.1", "Tourist@Test.dev");
            limiter.tryAcquire("10.0.0.1", "tourist@test.dev ");

            assertThat(counters).containsEntry("login-rate:account:10.0.0.1:tourist@test.dev", 2L);
            verify(redis, times(1)).expire(eq("login-rate:account:10.0.0.1:tourist@test.dev"), eq(Duration.ofSeconds(60)));
        }

        @Test
        void failsOpenWhenRedisIsDown() {
            when(ops.increment(anyString())).thenThrow(new RedisConnectionFailureException("down"));

            assertThat(limiter.tryAcquire("10.0.0.1", "tourist@test.dev")).isTrue();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class EmptyAdminLists {
        @Mock ProviderRepository providerRepository;
        @InjectMocks AuthServiceImpl authService;

        @Test
        void noPendingProvidersReturnsAnEmptyList() {
            when(providerRepository.findByAccommodationApprovalStatusOrTourGuideApprovalStatusOrTransportApprovalStatusOrActivityApprovalStatusOrFoodApprovalStatus(
                    any(ApprovalStatus.class), any(ApprovalStatus.class), any(ApprovalStatus.class),
                    any(ApprovalStatus.class), any(ApprovalStatus.class)))
                    .thenReturn(List.of());

            APIResponse<ProviderInfoResponse> response = authService.getBasicProviderInfo();

            assertThat(response.getData().getContent()).isNotNull().isEmpty();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class EmptyComplaints {
        @Mock ComplaintRepository complaintRepository;
        @InjectMocks ComplaintServiceImpl complaintService;

        @Test
        void noComplaintsReturnsAnEmptyList() {
            when(complaintRepository.findByComplaintStatusOrComplaintStatusOrComplaintStatus(
                    ComplaintStatus.PENDING, ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED))
                    .thenReturn(List.of());

            APIResponse<ComplaintInfoResponse> response = complaintService.getAllComplaints();

            assertThat(response.getData().getContent()).isNotNull().isEmpty();
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class PreferencesUpdate {
        @Mock UserRepository userRepository;
        @Mock AuthUtils authUtils;
        @Spy ModelMapper modelMapper = new ModelMapper();
        @InjectMocks UserServiceimpl userService;

        @Test
        void updatesTheExistingRowAndKeepsFieldsThatWereNotSent() {
            Tourist user = new Tourist();
            user.setUserId(3L);
            UserPreferences existing = new UserPreferences();
            existing.setId(40L);
            existing.setUser(user);
            existing.setPreferredCurrency("USD");
            existing.setPushNotifications(true);
            existing.setEmailNotifications(true);
            user.setPreferences(existing);
            when(authUtils.loggedInUserId()).thenReturn(3L);
            when(userRepository.findById(3L)).thenReturn(Optional.of(user));

            UserPreferencesUpdateRequest request = new UserPreferencesUpdateRequest();
            request.setPushNotifications(false);
            APIResponse<UserPreferencesDTO> response = userService.updateUserPreferences(request);

            assertThat(user.getPreferences()).isSameAs(existing);
            assertThat(existing.getUser()).isSameAs(user);
            assertThat(existing.getPushNotifications()).isFalse();
            assertThat(existing.getEmailNotifications()).isTrue();
            assertThat(existing.getPreferredCurrency()).isEqualTo("USD");
            assertThat(response.getData().getPushNotifications()).isFalse();
            verify(userRepository).save(user);
        }

        @Test
        void createsALinkedRowWhenTheUserHasNone() {
            Tourist user = new Tourist();
            user.setUserId(4L);
            when(authUtils.loggedInUserId()).thenReturn(4L);
            when(userRepository.findById(4L)).thenReturn(Optional.of(user));

            UserPreferencesUpdateRequest request = new UserPreferencesUpdateRequest();
            request.setNotificationsEnabled(true);
            userService.updateUserPreferences(request);

            assertThat(user.getPreferences()).isNotNull();
            assertThat(user.getPreferences().getUser()).isSameAs(user);
            assertThat(user.getPreferences().getNotificationsEnabled()).isTrue();
        }
    }

}
