package com.lankatrails.lankatrails_backend.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankatrails.lankatrails_backend.model.Tourist;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.security.jwt.JwtUtils;
import com.lankatrails.lankatrails_backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenRedisService refreshSvc;

    @Autowired
    OAuthUserService oAuthUserService;
    // your service to find/create users

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {
        DefaultOAuth2User oauthUser = (DefaultOAuth2User) auth.getPrincipal();
        String email = oauthUser.getAttribute("email");
        Map<String, Object> attributes = oauthUser.getAttributes();
        log.info("OAuth2 login success for email: {}", email);

        // 1. Find or create a Tourist in your DB
        User user = oAuthUserService.findOrCreateOAuthUser(email, attributes);

        if (!(user instanceof Tourist)) {
            log.error("OAuth2 login failed: User is not a Tourist");
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "User is not a Tourist");
            return;
        }

        // 2. Generate your JWT + refresh token
        UserDetailsImpl ud = UserDetailsImpl.build(user);
        String accessToken = jwtUtils.generateToken(ud);
        String refreshToken = jwtUtils.generateRefreshToken(ud);
        refreshSvc.storeToken(user.getEmail(), refreshToken);

        log.info("Generated access token and refresh token for user: {}", user.getEmail());

        // 3. Return tokens in JSON body
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(res.getOutputStream(), Map.of(
                "jwtToken", accessToken,
                "refreshToken", refreshToken,
                "id", user.getUserId(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "firstName", ((Tourist) user).getFirstName(),
                "lastName", ((Tourist) user).getLastName(),
                "country", ((Tourist) user).getCountry()
        ));
    }
}

