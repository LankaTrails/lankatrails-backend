package com.lankatrails.lankatrails_backend.service;

import com.lankatrails.lankatrails_backend.dtos.request.DeactivateAccountRequest;
import com.lankatrails.lankatrails_backend.exception.BadCredentialsException;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import com.lankatrails.lankatrails_backend.security.service.RefreshTokenRedisService;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.impl.UserServiceimpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountDeactivationTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RefreshTokenRedisService refreshTokenRedisService;
    @Mock AuthUtils authUtils;
    @InjectMocks UserServiceimpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(7L);
        user.setEmail("provider@test.dev");
        user.setPassword("hashed");
        user.setStatus(UserStatus.ACTIVE);
        when(authUtils.loggedInUserId()).thenReturn(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
    }

    @Test
    void disablesAccountAndRevokesRefreshToken() {
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

        var response = userService.deactivateAccount(request("secret", "Closing the business"));

        assertThat(response.isSuccess()).isTrue();
        assertThat(user.getStatus()).isEqualTo(UserStatus.DISABLED);
        verify(userRepository).save(user);
        verify(refreshTokenRedisService).deleteToken("provider@test.dev");
    }

    @Test
    void rejectsWrongPasswordWithoutChangingAnything() {
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.deactivateAccount(request("wrong", null)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Password is incorrect.");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository, never()).save(any());
        verify(refreshTokenRedisService, never()).deleteToken(anyString());
    }

    private static DeactivateAccountRequest request(String password, String reason) {
        DeactivateAccountRequest request = new DeactivateAccountRequest();
        request.setPassword(password);
        request.setReason(reason);
        return request;
    }
}
