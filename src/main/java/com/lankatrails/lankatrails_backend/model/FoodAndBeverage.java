package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "food_and_beverage")
@Getter
@Setter
@NoArgsConstructor
public class FoodAndBeverage extends Service{
    private String openHours;
}
