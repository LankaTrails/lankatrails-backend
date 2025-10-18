package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "food_and_beverage")
@Getter
@Setter
@NoArgsConstructor
public class FoodAndBeverage extends Service{

    @Column(name = "vegetarian_options")
    private Boolean vegetarianOptions;

    @Column(name = "halal_certified")
    private Boolean halalCertified;

    @Column(name = "alcohol_served")
    private Boolean alcoholServed;

    @Column(name = "outdoor_seating")
    private Boolean outdoorSeating;

    @Column(name = "live_music")
    private Boolean liveMusic;

    @Column(name = "cuisine_type")
    private String cuisineType;

    @ManyToOne
    @JoinColumn(name = "foodAndBeverageCategory_id")
    private FoodAndBeverageCategory foodAndBeverageCategory;
}
