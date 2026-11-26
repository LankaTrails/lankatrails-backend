package com.lankatrails.lankatrails_backend.security;

import com.lankatrails.lankatrails_backend.config.WebSecurityConfig;
import com.lankatrails.lankatrails_backend.controller.AccommodationController;
import com.lankatrails.lankatrails_backend.controller.AdminController;
import com.lankatrails.lankatrails_backend.controller.LocationController;
import com.lankatrails.lankatrails_backend.controller.ReviewController;
import com.lankatrails.lankatrails_backend.controller.TripController;
import com.lankatrails.lankatrails_backend.controller.UserController;
import com.lankatrails.lankatrails_backend.dtos.response.APIResponse;
import com.lankatrails.lankatrails_backend.model.License;
import com.lankatrails.lankatrails_backend.model.Provider;
import com.lankatrails.lankatrails_backend.model.Service;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.repositories.LicenseRepository;
import com.lankatrails.lankatrails_backend.repositories.PolicySectionRepository;
import com.lankatrails.lankatrails_backend.repositories.ProviderRepository;
import com.lankatrails.lankatrails_backend.repositories.ServiceRepository;
import com.lankatrails.lankatrails_backend.repositories.TabsSectionRepository;
import com.lankatrails.lankatrails_backend.security.jwt.AuthEntryPointJwt;
import com.lankatrails.lankatrails_backend.security.jwt.AuthTokenFilter;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import com.lankatrails.lankatrails_backend.security.service.OwnershipService;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsServiceImpl;
import com.lankatrails.lankatrails_backend.security.utils.AuthUtils;
import com.lankatrails.lankatrails_backend.service.AccommodationService;
import com.lankatrails.lankatrails_backend.service.AuthService;
import com.lankatrails.lankatrails_backend.service.BookingService;
import com.lankatrails.lankatrails_backend.service.LocationService;
import com.lankatrails.lankatrails_backend.service.ReviewService;
import com.lankatrails.lankatrails_backend.service.TripItemService;
import com.lankatrails.lankatrails_backend.service.TripService;
import com.lankatrails.lankatrails_backend.service.UserService;
import com.lankatrails.lankatrails_backend.service.impl.PolicyImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real security filter chain, JWT filter, method security and ownership checks
 * against a slice of controllers whose services are mocked.
 */
@WebMvcTest(controllers = {
        AdminController.class,
        AccommodationController.class,
        UserController.class,
        ReviewController.class,
        LocationController.class,
        TripController.class
})
@Import({WebSecurityConfig.class, AuthEntryPointJwt.class, AuthTokenFilter.class, JwtUtils.class, OwnershipService.class})
@TestPropertySource(properties = {
        "spring.app.jwtSecret=bGFua2F0cmFpbHMtdGVzdC1vbmx5LXNpZ25pbmcta2V5LTAxMjM0NTY3ODktYWJjZGVmZ2hpamtsbW5vcA==",
        "spring.app.jwtExpirationMs=3600000",
        "spring.app.jwtRefreshExpirationMs=604800000",
        "spring.app.emailVerificationTokenExpirationMs=900000",
        "spring.app.jwtCookieName=lankatrails_jwt",
        "spring.app.refreshTokenCookieName=lankatrails_refresh_token",
        "app.cors.allowed-origins=http://localhost:5173"
})
class SecurityAccessTest {

    private static final long TOURIST_ID = 1L;
    private static final long PROVIDER_ID = 10L;
    private static final long OTHER_PROVIDER_ID = 20L;
    private static final long ADMIN_ID = 99L;
    private static final long OWN_SERVICE_ID = 100L;
    private static final long OTHER_SERVICE_ID = 200L;
    private static final String OWN_LICENCE = "/uploads/licence/own.pdf";
    private static final String OTHER_LICENCE = "/uploads/licence/someone-else.pdf";

    @Autowired private MockMvc mvc;
    @Autowired private JwtUtils jwtUtils;

    // Controller collaborators
    @MockitoBean private AuthService authService;
    @MockitoBean private BookingService bookingService;
    @MockitoBean private AccommodationService accommodationService;
    @MockitoBean private PolicyImpl policyImpl;
    @MockitoBean private AuthUtils authUtils;
    @MockitoBean private UserService userService;
    @MockitoBean private ReviewService reviewService;
    @MockitoBean private LocationService locationService;
    @MockitoBean private TripService tripService;
    @MockitoBean private TripItemService tripItemService;

    // Security collaborators
    @MockitoBean private UserDetailsServiceImpl userDetailsService;
    @MockitoBean private ServiceRepository serviceRepository;
    @MockitoBean private PolicySectionRepository policySectionRepository;
    @MockitoBean private TabsSectionRepository tabsSectionRepository;
    @MockitoBean private ProviderRepository providerRepository;
    @MockitoBean private LicenseRepository licenseRepository;

    private final UserDetailsImpl tourist = principal(TOURIST_ID, "tourist@test.dev", UserRole.ROLE_TOURIST);
    private final UserDetailsImpl provider = principal(PROVIDER_ID, "provider@test.dev", UserRole.ROLE_PROVIDER);
    private final UserDetailsImpl admin = principal(ADMIN_ID, "admin@test.dev", UserRole.ROLE_ADMIN);

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void stubData() {
        APIResponse<Object> ok = APIResponse.builder().success(true).build();
        when(authService.getBasicProviderInfo()).thenReturn((APIResponse) ok);
        when(accommodationService.searchWithId(anyLong())).thenReturn((APIResponse) ok);
        when(accommodationService.getAll_Accommodations(any(), any())).thenReturn((APIResponse) ok);
        when(accommodationService.deactivateService(anyLong())).thenReturn((APIResponse) ok);
        when(reviewService.getReviewsByServiceId(anyLong())).thenReturn((APIResponse) ok);
        when(locationService.getAllCities()).thenReturn((APIResponse) ok);
        when(userService.addProfilePicture(anyLong(), any())).thenReturn((APIResponse) ok);
        when(tripService.getAllMyTrips()).thenReturn((APIResponse) APIResponse.builder().success(true).data(List.of()).build());

        when(serviceRepository.findById(OWN_SERVICE_ID)).thenReturn(Optional.of(service(OWN_SERVICE_ID, PROVIDER_ID)));
        when(serviceRepository.findById(OTHER_SERVICE_ID)).thenReturn(Optional.of(service(OTHER_SERVICE_ID, OTHER_PROVIDER_ID)));

        Provider self = new Provider();
        self.setUserId(PROVIDER_ID);
        when(providerRepository.findByUserId(PROVIDER_ID)).thenReturn(Optional.of(self));
        License licence = new License();
        licence.setLicenseUrl(OWN_LICENCE);
        when(licenseRepository.findByProvider_UserId(PROVIDER_ID)).thenReturn(List.of(licence));

        when(userDetailsService.loadUserByUsername(tourist.getEmail())).thenReturn(tourist);
    }

    @Nested
    class PublicEndpoints {
        @Test
        void serviceDetailsAreOpenToAnonymousUsers() throws Exception {
            mvc.perform(get("/api/provider/accommodation/{id}", OWN_SERVICE_ID)).andExpect(status().isOk());
        }

        @Test
        void reviewsCanBeReadAnonymously() throws Exception {
            mvc.perform(get("/api/reviews/{id}", OWN_SERVICE_ID)).andExpect(status().isOk());
        }

        @Test
        void locationsAreOpenToAnonymousUsers() throws Exception {
            mvc.perform(get("/api/locations/cities")).andExpect(status().isOk());
        }

        @Test
        void writingAReviewRequiresAuthentication() throws Exception {
            mvc.perform(post("/api/reviews/{id}", OWN_SERVICE_ID)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"rate\":5}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class AdminArea {
        private final MockHttpServletRequestBuilder request = get("/api/admin/approve-provider/providers");

        @Test
        void anonymousIsUnauthorized() throws Exception {
            mvc.perform(request).andExpect(status().isUnauthorized());
        }

        @Test
        void touristIsForbidden() throws Exception {
            mvc.perform(request.with(user(tourist))).andExpect(status().isForbidden());
        }

        @Test
        void providerIsForbidden() throws Exception {
            mvc.perform(request.with(user(provider))).andExpect(status().isForbidden());
        }

        @Test
        void adminIsAllowed() throws Exception {
            mvc.perform(request.with(user(admin))).andExpect(status().isOk());
        }
    }

    @Nested
    class ProviderArea {
        private final MockHttpServletRequestBuilder listOwnServices = get("/api/provider/accommodation/getAll")
                .param("pageNumber", "0").param("pageSize", "10");

        @Test
        void anonymousIsUnauthorized() throws Exception {
            mvc.perform(listOwnServices).andExpect(status().isUnauthorized());
        }

        @Test
        void touristIsForbidden() throws Exception {
            mvc.perform(listOwnServices.with(user(tourist))).andExpect(status().isForbidden());
        }

        @Test
        void providerIsAllowed() throws Exception {
            mvc.perform(listOwnServices.with(user(provider))).andExpect(status().isOk());
        }

        @Test
        void providerCanChangeOwnService() throws Exception {
            mvc.perform(put("/api/provider/accommodation/deactivate/{id}", OWN_SERVICE_ID).with(user(provider)))
                    .andExpect(status().isOk());
        }

        @Test
        void providerCannotChangeAnotherProvidersService() throws Exception {
            mvc.perform(put("/api/provider/accommodation/deactivate/{id}", OTHER_SERVICE_ID).with(user(provider)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class TouristArea {
        @Test
        void anonymousIsUnauthorized() throws Exception {
            mvc.perform(get("/api/trips/my-trips")).andExpect(status().isUnauthorized());
        }

        @Test
        void providerIsForbidden() throws Exception {
            mvc.perform(get("/api/trips/my-trips").with(user(provider))).andExpect(status().isForbidden());
        }

        @Test
        void touristIsAllowed() throws Exception {
            mvc.perform(get("/api/trips/my-trips").with(user(tourist))).andExpect(status().isOk());
        }
    }

    @Nested
    class UserProfilePicture {
        private final MockMultipartFile picture =
                new MockMultipartFile("profilePicture", "me.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        @Test
        void anonymousIsUnauthorized() throws Exception {
            mvc.perform(multipart("/api/user/{id}/add-profile-picture", TOURIST_ID).file(picture))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void userCanUpdateOwnPicture() throws Exception {
            mvc.perform(multipart("/api/user/{id}/add-profile-picture", TOURIST_ID).file(picture).with(user(tourist)))
                    .andExpect(status().isOk());
        }

        @Test
        void userCannotUpdateSomeoneElsesPicture() throws Exception {
            mvc.perform(multipart("/api/user/{id}/add-profile-picture", PROVIDER_ID).file(picture).with(user(tourist)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class PrivateUploads {
        @Test
        void anonymousCannotReadIdentityDocuments() throws Exception {
            mvc.perform(get(OWN_LICENCE)).andExpect(status().isUnauthorized());
        }

        @Test
        void touristCannotReadIdentityDocuments() throws Exception {
            mvc.perform(get(OWN_LICENCE).with(user(tourist))).andExpect(status().isForbidden());
        }

        @Test
        void providerCannotReadAnotherProvidersDocuments() throws Exception {
            mvc.perform(get(OTHER_LICENCE).with(user(provider))).andExpect(status().isForbidden());
        }

        @Test
        void providerCanReadOwnDocument() throws Exception {
            // Authorised: the file does not exist in the test workspace, so 404 rather than 401/403
            mvc.perform(get(OWN_LICENCE).with(user(provider))).andExpect(status().isNotFound());
        }

        @Test
        void adminCanReadAnyDocument() throws Exception {
            mvc.perform(get(OTHER_LICENCE).with(user(admin))).andExpect(status().isNotFound());
        }

        @Test
        void serviceImagesStayPublic() throws Exception {
            mvc.perform(get("/uploads/service-pictures/missing.jpg")).andExpect(status().isNotFound());
        }
    }

    @Nested
    class TokenTypes {
        @Test
        void accessTokenAuthenticates() throws Exception {
            mvc.perform(get("/api/trips/my-trips").header("Authorization", "Bearer " + jwtUtils.generateToken(tourist)))
                    .andExpect(status().isOk());
        }

        @Test
        void refreshTokenIsRejectedAsAccessToken() throws Exception {
            mvc.perform(get("/api/trips/my-trips").header("Authorization", "Bearer " + jwtUtils.generateRefreshToken(tourist)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void emailVerificationTokenIsRejectedAsAccessToken() throws Exception {
            User user = new User();
            user.setUserId(TOURIST_ID);
            user.setEmail(tourist.getEmail());
            mvc.perform(get("/api/trips/my-trips").header("Authorization", "Bearer " + jwtUtils.generateEmailVerificationJwt(user)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void tamperedTokenIsRejected() throws Exception {
            String token = jwtUtils.generateToken(tourist);
            mvc.perform(get("/api/trips/my-trips").header("Authorization", "Bearer " + token.substring(0, token.length() - 4) + "abcd"))
                    .andExpect(status().isUnauthorized());
        }
    }

    private static UserDetailsImpl principal(long id, String email, UserRole role) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setPassword("n/a");
        user.setEmailVerified(true);
        user.setRole(role);
        return UserDetailsImpl.build(user);
    }

    private static Service service(long serviceId, long ownerId) {
        Provider owner = new Provider();
        owner.setUserId(ownerId);
        Service service = new Service();
        service.setServiceId(serviceId);
        service.setProvider(owner);
        return service;
    }
}
