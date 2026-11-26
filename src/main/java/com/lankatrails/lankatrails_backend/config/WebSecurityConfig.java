package com.lankatrails.lankatrails_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankatrails.lankatrails_backend.security.jwt.AuthEntryPointJwt;
import com.lankatrails.lankatrails_backend.security.jwt.AuthTokenFilter;
import com.lankatrails.lankatrails_backend.security.service.OwnershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    /** Service detail pages are public so tourists can browse without an account. */
    private static final String[] PUBLIC_SERVICE_DETAILS = {
            "/api/provider/accommodation/{id:\\d+}",
            "/api/provider/activity-service/{id:\\d+}",
            "/api/provider/food-beverage/{id:\\d+}",
            "/api/provider/tour-guide/{id:\\d+}",
            "/api/provider/transport/{id:\\d+}"
    };

    /** Upload folders holding identity and business documents. Everything else under /uploads is public. */
    private static final String[] PRIVATE_UPLOADS = {
            "/uploads/documents/**",
            "/uploads/business-registration/**",
            "/uploads/identification/**",
            "/uploads/licence/**"
    };

    private final AuthTokenFilter authTokenFilter;
    private final AuthEntryPointJwt authEntryPointJwt;
    private final OwnershipService ownershipService;

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPointJwt)
                        .accessDeniedHandler(jsonAccessDeniedHandler())
                )
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(63072000)
                        )
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'")
                        )
                        // Dashboards render uploaded PDFs in iframes
                        .frameOptions(frame -> frame.disable())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Authentication: public, except endpoints bound to the current session
                        .requestMatchers("/api/auth/logged-user", "/api/auth/change-password", "/api/auth/logout").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public catalogue
                        .requestMatchers("/api/service/**").permitAll()
                        .requestMatchers("/api/locations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_SERVICE_DETAILS).permitAll()

                        // Stripe calls this directly; requests are verified by signature
                        .requestMatchers("/api/webhook/stripe/**").permitAll()
                        // SockJS handshake; STOMP CONNECT is authenticated by StompAuthChannelInterceptor
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()

                        // Uploaded files
                        .requestMatchers(PRIVATE_UPLOADS).access(adminOrDocumentOwner())
                        .requestMatchers("/uploads/**").permitAll()

                        // Role areas
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/provider/**").hasRole("PROVIDER")
                        .requestMatchers("/api/external-bookings/**").hasRole("PROVIDER")
                        .requestMatchers("/api/tourist/**", "/api/trip/**", "/api/trips/**", "/api/trips-budget/**").hasRole("TOURIST")
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthTokenFilter is a @Component so it can be injected elsewhere (STOMP auth). Without this,
     * Spring Boot would also register it as a servlet filter and run it twice per request.
     */
    @Bean
    public FilterRegistrationBean<AuthTokenFilter> authTokenFilterRegistration(AuthTokenFilter filter) {
        FilterRegistrationBean<AuthTokenFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    private AuthorizationManager<RequestAuthorizationContext> adminOrDocumentOwner() {
        return (authentication, context) -> {
            Authentication auth = authentication.get();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return new AuthorizationDecision(false);
            }
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            return new AuthorizationDecision(
                    isAdmin || ownershipService.isOwnPrivateDocument(context.getRequest().getRequestURI()));
        };
    }

    private AccessDeniedHandler jsonAccessDeniedHandler() {
        ObjectMapper mapper = new ObjectMapper();
        return (request, response, ex) -> {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mapper.writeValue(response.getOutputStream(), Map.of(
                    "status", 403,
                    "error", "Forbidden",
                    "message", "You do not have permission to access this resource",
                    "path", request.getServletPath()
            ));
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setExposedHeaders(Arrays.asList("Authorization", "content-type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/error", "/favicon.ico");
    }
}
