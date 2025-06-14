package com.lankatrails.lankatrails_backend.config;

import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InitialDataConfig {

    @Bean
    CommandLineRunner initCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                // Add initial categories
                categoryRepository.save(new Category(ServiceCategory.ACCOMMODATION));
                categoryRepository.save(new Category(ServiceCategory.TRANSPORT));
                categoryRepository.save(new Category(ServiceCategory.FOOD_BEVERAGE));
                categoryRepository.save(new Category(ServiceCategory.ACTIVITY));
                categoryRepository.save(new Category(ServiceCategory.TOUR_GUIDE));
                System.out.println("Initial categories created");
            }
        };
    }
}