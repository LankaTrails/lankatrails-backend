package com.lankatrails.lankatrails_backend;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base test class that provides common test configuration for all integration tests.
 * Extend this class for tests that need the full Spring application context.
 * 
 * This class combines both test configuration and mock bean definitions for a 
 * streamlined testing setup.
 */
@SpringBootTest(
    classes = {LankatrailsBackendApplication.class, BaseTest.TestConfig.class},
    properties = {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration"}
)
@ActiveProfiles("test")
public abstract class BaseTest {
    
    /**
     * Inner test configuration class that provides test-specific mock beans.
     * This eliminates the need for a separate TestConfig file.
     */
    @TestConfiguration
    static class TestConfig {

        /**
         * Provides a mock Redis connection factory for testing
         * when Redis auto-configuration is disabled.
         */
        @Bean
        @Primary
        public RedisConnectionFactory redisConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }

        /**
         * Provides a mock RedisTemplate for testing
         * when Redis auto-configuration is disabled.
         */
        @Bean
        @Primary
        public RedisTemplate<String, Object> redisTemplate() {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory());
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            return template;
        }

        /**
         * Provides a mock RedisTemplate for String-String operations
         * required by RefreshTokenRedisService
         */
        @Bean
        @Primary
        public RedisTemplate<String, String> stringRedisTemplate() {
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(redisConnectionFactory());
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            return template;
        }

        /**
         * Provides a simple JavaMailSender implementation for testing
         * email functionality without requiring a real SMTP server.
         */
        @Bean
        @Primary
        public JavaMailSender javaMailSender() {
            return new JavaMailSenderImpl();
        }

        /**
         * Provides a mock MongoDB template for testing
         * when MongoDB auto-configuration is disabled.
         */
        @Bean
        @Primary
        @SuppressWarnings({"unchecked", "rawtypes"})
        public org.springframework.data.mongodb.core.MongoTemplate mongoTemplate() {
            org.springframework.data.mongodb.core.MongoTemplate mongoTemplate = mock(org.springframework.data.mongodb.core.MongoTemplate.class);
            org.springframework.data.mongodb.core.convert.MongoConverter converter = mock(org.springframework.data.mongodb.core.convert.MongoConverter.class);
            org.springframework.data.mapping.context.MappingContext mappingContext = mock(org.springframework.data.mapping.context.MappingContext.class);
            
            when(mongoTemplate.getConverter()).thenReturn(converter);
            when(converter.getMappingContext()).thenReturn(mappingContext);
            
            return mongoTemplate;
        }
    }
    
    // Common test setup and utilities can be added here
}