package com.lankatrails.lankatrails_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accommodation")
@Getter
@Setter
@NoArgsConstructor
public class Accommodation extends Service {

    private String about;

    @ManyToOne
    @JoinColumn(name = "accommodationCategory_id")
    private AccommodationCategory accommodationCategory;




}
