package com.lankatrails.lankatrails_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lankatrails.lankatrails_backend.model.AccommodationCategory;
import com.lankatrails.lankatrails_backend.model.ActivityCategory;
import com.lankatrails.lankatrails_backend.model.Admin;
import com.lankatrails.lankatrails_backend.model.Category;
import com.lankatrails.lankatrails_backend.model.FoodAndBeverageCategory;
import com.lankatrails.lankatrails_backend.model.TourGuideCategory;
import com.lankatrails.lankatrails_backend.model.TripTag;
import com.lankatrails.lankatrails_backend.model.VehicleCategory;
import com.lankatrails.lankatrails_backend.model.enums.AccommodationType;
import com.lankatrails.lankatrails_backend.model.enums.ActivityType;
import com.lankatrails.lankatrails_backend.model.enums.FoodAndBeverageType;
import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.TourGuideType;
import com.lankatrails.lankatrails_backend.model.enums.TripTagType;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import com.lankatrails.lankatrails_backend.model.enums.VehicleType;
import com.lankatrails.lankatrails_backend.repositories.AccommodationCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.ActivityCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.CategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.FoodAndBeverageCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.TourGuideCategoryRepository;
import com.lankatrails.lankatrails_backend.repositories.TripTagRepository;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import com.lankatrails.lankatrails_backend.repositories.VehicleCategoryRepository;

@Configuration
public class InitialDataConfig {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               PasswordEncoder passwordEncoder,
                               AccommodationCategoryRepository accommodationCategoryRepository,
                               VehicleCategoryRepository vehicleCategoryRepository,
                               ActivityCategoryRepository activityCategoryRepository,
                               TourGuideCategoryRepository tourGuideCategoryRepository,
                               FoodAndBeverageCategoryRepository foodBeverageCategoryRepository,
                               TripTagRepository tripTagRepository) {
        return args -> {
            initAdmin(userRepository, passwordEncoder);
            initCategories(categoryRepository);
            initAccommodationTypes(accommodationCategoryRepository);
            initVehicleTypes(vehicleCategoryRepository);
            initActivityTypes(activityCategoryRepository);
            initTourGuideTypes(tourGuideCategoryRepository);
            initFoodBeverageTypes(foodBeverageCategoryRepository);
            initTripTags(tripTagRepository);
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
            admin.setEmailVerified(true);

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

    private void initAccommodationTypes(AccommodationCategoryRepository accommodationCategoryRepository) {
        if (accommodationCategoryRepository.count() == 0) {
            accommodationCategoryRepository.save(new AccommodationCategory(AccommodationType.HOTEL));
            accommodationCategoryRepository.save(new AccommodationCategory(AccommodationType.HOSTEL));
            accommodationCategoryRepository.save(new AccommodationCategory(AccommodationType.GUEST_HOUSE));
            accommodationCategoryRepository.save(new AccommodationCategory(AccommodationType.VILLA));
            accommodationCategoryRepository.save(new AccommodationCategory(AccommodationType.APARTMENT));
            accommodationCategoryRepository.save(new AccommodationCategory(AccommodationType.RESORT));
            accommodationCategoryRepository.save(new AccommodationCategory(AccommodationType.HOMESTAY));
            accommodationCategoryRepository.save(new AccommodationCategory(AccommodationType.CAMPING));
            accommodationCategoryRepository.save(new AccommodationCategory(AccommodationType.LODGE));
            System.out.println("Initial accommodation types created");
        }
    }

    private void initVehicleTypes(VehicleCategoryRepository vehicleTypeRepository) {
        if (vehicleTypeRepository.count() == 0) {
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.CAR));
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.VAN));
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.BUS));
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.TRUCK));
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.MOTORCYCLE));
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.BICYCLE));
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.SCOOTER));
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.PICKUP));
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.SUV));
            vehicleTypeRepository.save(new VehicleCategory(VehicleType.TUK_TUK));
            System.out.println("Initial vehicle types created");
        }
    }

    private void initActivityTypes(ActivityCategoryRepository activityCategoryRepository) {
        if (activityCategoryRepository.count() == 0) {
            activityCategoryRepository.save(new ActivityCategory(ActivityType.ADVENTURE));
            activityCategoryRepository.save(new ActivityCategory(ActivityType.EDUCATIONAL));
            activityCategoryRepository.save(new ActivityCategory(ActivityType.CULTURAL));
            activityCategoryRepository.save(new ActivityCategory(ActivityType.NATURE));
            activityCategoryRepository.save(new ActivityCategory(ActivityType.RELAXATION));
            activityCategoryRepository.save(new ActivityCategory(ActivityType.SPORTS));
            activityCategoryRepository.save(new ActivityCategory(ActivityType.WATER_SPORTS));
            activityCategoryRepository.save(new ActivityCategory(ActivityType.NIGHTLIFE));
            activityCategoryRepository.save(new ActivityCategory(ActivityType.WELLNESS));
            System.out.println("Initial activity types created");
        }
    }

    private void initTourGuideTypes(TourGuideCategoryRepository tourGuideCategoryRepository) {
        if (tourGuideCategoryRepository.count() == 0) {
            tourGuideCategoryRepository.save(new TourGuideCategory(TourGuideType.NATIONAL));
            tourGuideCategoryRepository.save(new TourGuideCategory(TourGuideType.CHAUFFEUR));
            tourGuideCategoryRepository.save(new TourGuideCategory(TourGuideType.SITE));
            tourGuideCategoryRepository.save(new TourGuideCategory(TourGuideType.AREA));
            System.out.println("Initial tour guide types created");
        }
    }

    private void initFoodBeverageTypes(FoodAndBeverageCategoryRepository foodBeverageCategoryRepository) {
        if (foodBeverageCategoryRepository.count() == 0) {
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.RESTAURANT));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.CAFE));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.BAR));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.STREET_FOOD));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.FOOD_TRUCK));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.BAKERY));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.BREWERY));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.BUFFET));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.PUB));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.WINERY));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.DISTILLERY));
            foodBeverageCategoryRepository.save(new FoodAndBeverageCategory(FoodAndBeverageType.FOOD_COURT));
            System.out.println("Initial food and beverage types created");
        }
    }

    private void initTripTags(TripTagRepository tripTagRepository) {
        if (tripTagRepository.count() == 0) {
            tripTagRepository.save(new TripTag(TripTagType.HONEYMOON));
            tripTagRepository.save(new TripTag(TripTagType.FAMILY));
            tripTagRepository.save(new TripTag(TripTagType.ADVENTURE));
            tripTagRepository.save(new TripTag(TripTagType.CULTURAL));
            tripTagRepository.save(new TripTag(TripTagType.BUSINESS));
            tripTagRepository.save(new TripTag(TripTagType.EDUCATIONAL));
            tripTagRepository.save(new TripTag(TripTagType.RELAXATION));
            tripTagRepository.save(new TripTag(TripTagType.SOLO));
            tripTagRepository.save(new TripTag(TripTagType.GROUP));
            tripTagRepository.save(new TripTag(TripTagType.ROMANTIC));
            tripTagRepository.save(new TripTag(TripTagType.NATURE));
            tripTagRepository.save(new TripTag(TripTagType.HISTORICAL));
            tripTagRepository.save(new TripTag(TripTagType.SPORTS));
            tripTagRepository.save(new TripTag(TripTagType.FESTIVAL));
            tripTagRepository.save(new TripTag(TripTagType.LEISURE));
            System.out.println("Initial trip tags created");
        }
    }

}