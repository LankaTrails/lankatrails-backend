package com.lankatrails.lankatrails_backend.config;

import com.lankatrails.lankatrails_backend.rabbit.Security.RateLimitingChannelInterceptor;
import com.lankatrails.lankatrails_backend.rabbit.Security.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.Duration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Value("${spring.rabbitmq.host}")
    private String relayHost;

    @Value("${spring.rabbitmq.stomp-port}")
    private int relayPort;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        // Use a dedicated, low-privilege RabbitMQ user for the relay.
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(relayHost)
                .setRelayPort(relayPort)
                .setSystemLogin(username)
                .setSystemPasscode(password)
                .setClientLogin(username)
                .setClientPasscode(password);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // It is recommended to externalize allowed origins to application.properties
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "https://*.lankatrails.com",
                        "https://lankatrails.com",
                        "http://localhost:3000",
                        "http://localhost:8081",
                        "http://localhost:8082",
                        "http://localhost:5500"
                )
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // The order of interceptors is critical for security.
        registration.interceptors(
                // 1. Authentication and Authorization Interceptor (stompAuthChannelInterceptor):
                //    - Runs first.
                //    - Authenticates the user on CONNECT using their JWT.
                //    - Authorizes SUBSCRIBE requests to ensure the user is a member of the requested chat room.
                //    - Populates the SecurityContext for all subsequent messages in the session.
                stompAuthChannelInterceptor,

                // 2. Per-User Rate Limiting Interceptor:
                //    - Runs after authentication.
                //    - Applies a rate limit to each user's session individually.
                //    - This prevents a single malicious or misbehaving user from overwhelming the system
                //      and affecting other users.
                new RateLimitingChannelInterceptor(10, Duration.ofSeconds(1)) // Example: 10 messages per second per user
        );
    }
}