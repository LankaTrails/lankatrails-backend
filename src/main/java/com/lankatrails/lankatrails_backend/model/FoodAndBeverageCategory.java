package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.FoodAndBeverageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "food_and_beverage_category")
@ToString
public class FoodAndBeverageCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer foodAndBeverageCategoryId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "name", nullable = false, unique = true, columnDefinition = "VARCHAR(20)")
    private FoodAndBeverageType categoryName;

    @OneToMany(mappedBy = "foodAndBeverageCategory")
    private Set<FoodAndBeverage> foodAndBeverages = new HashSet<>();

    public FoodAndBeverageCategory(FoodAndBeverageType foodAndBeverageType) {
        this.categoryName = foodAndBeverageType;
    }
}
