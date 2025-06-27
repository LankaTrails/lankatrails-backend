package com.lankatrails.lankatrails_backend.config;

import com.lankatrails.lankatrails_backend.model.Admin;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitialDataConfig {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            initAdmin(userRepository, passwordEncoder);
            initCategories(categoryRepository);
        };
    }

    private void initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (!userRepository.existsByRole(UserRole.ROLE_ADMIN)) {
            Admin admin = new Admin();
            admin.setEmail("admin@lankatrails.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setStatus(UserStatus.ACTIVE);

            userRepository.save(admin);
            System.out.println("Initial admin user created");
        }
    }

    private void initCategories(CategoryRepository categoryRepository) {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category(ServiceCategory.ACCOMMODATION));
            categoryRepository.save(new Category(ServiceCategory.TRANSPORT));
            categoryRepository.save(new Category(ServiceCategory.FOOD_BEVERAGE));
            categoryRepository.save(new Category(ServiceCategory.ACTIVITY));
            categoryRepository.save(new Category(ServiceCategory.TOUR_GUIDE));
            System.out.println("Initial service categories created");
        }
    }
}