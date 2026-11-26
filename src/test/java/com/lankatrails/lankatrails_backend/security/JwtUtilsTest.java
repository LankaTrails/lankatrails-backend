package com.lankatrails.lankatrails_backend.security;

import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private static final long ACCESS_MS = 3_600_000L;
    private static final long REFRESH_MS = 604_800_000L;

    private JwtUtils jwtUtils;
    private User user;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                "bGFua2F0cmFpbHMtdGVzdC1vbmx5LXNpZ25pbmcta2V5LTAxMjM0NTY3ODktYWJjZGVmZ2hpamtsbW5vcA==");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", ACCESS_MS);
        ReflectionTestUtils.setField(jwtUtils, "jwtRefreshExpirationMs", REFRESH_MS);
        ReflectionTestUtils.setField(jwtUtils, "emailVerificationTokenExpirationMs", 900_000);
        ReflectionTestUtils.setField(jwtUtils, "jwtCookie", "lankatrails_jwt");
        ReflectionTestUtils.setField(jwtUtils, "refreshTokenCookie", "lankatrails_refresh_token");

        user = new User();
        user.setUserId(1L);
        user.setEmail("tourist@test.dev");
        user.setPassword("n/a");
        user.setEmailVerified(false);
        user.setRole(UserRole.ROLE_TOURIST);
        userDetails = UserDetailsImpl.build(user);
    }

    @Test
    void emailVerificationTokenIsAcceptedOnlyForEmailVerification() {
        String token = jwtUtils.generateEmailVerificationJwt(user);

        assertThat(jwtUtils.getUserNameFromJwtToken(token, JwtUtils.TYPE_EMAIL_VERIFICATION)).isEqualTo("tourist@test.dev");
        assertThat(jwtUtils.getUserNameFromJwtToken(token, JwtUtils.TYPE_ACCESS)).isNull();
        assertThat(jwtUtils.getUserNameFromJwtToken(token, JwtUtils.TYPE_REFRESH)).isNull();
    }

    @Test
    void accessAndRefreshTokensCannotVerifyAnEmail() {
        assertThat(jwtUtils.getUserNameFromJwtToken(jwtUtils.generateToken(userDetails), JwtUtils.TYPE_EMAIL_VERIFICATION)).isNull();
        assertThat(jwtUtils.getUserNameFromJwtToken(jwtUtils.generateRefreshToken(userDetails), JwtUtils.TYPE_EMAIL_VERIFICATION)).isNull();
    }

    @Test
    void refreshTokenIsAcceptedOnlyForRefresh() {
        String refresh = jwtUtils.generateRefreshToken(userDetails);

        assertThat(jwtUtils.getUserNameFromJwtToken(refresh, JwtUtils.TYPE_REFRESH)).isEqualTo("tourist@test.dev");
        assertThat(jwtUtils.getUserNameFromJwtToken(refresh, JwtUtils.TYPE_ACCESS)).isNull();
    }

    @Test
    void garbageTokensAreRejected() {
        assertThat(jwtUtils.getUserNameFromJwtToken("not-a-jwt", JwtUtils.TYPE_ACCESS)).isNull();
        assertThat(jwtUtils.getUserNameFromJwtToken("", JwtUtils.TYPE_ACCESS)).isNull();
        assertThat(jwtUtils.getUserNameFromJwtToken(null, JwtUtils.TYPE_ACCESS)).isNull();
    }

    @Test
    void cookieLifetimesMatchTokenLifetimes() {
        ResponseCookie access = jwtUtils.generateJwtCookie("a");
        ResponseCookie refresh = jwtUtils.generateRefreshCookie("r");

        assertThat(access.getMaxAge().toMillis()).isEqualTo(ACCESS_MS);
        assertThat(refresh.getMaxAge().toMillis()).isEqualTo(REFRESH_MS);
        assertThat(jwtUtils.getJwtRefreshExpirationMs()).isEqualTo(REFRESH_MS);
    }
}
