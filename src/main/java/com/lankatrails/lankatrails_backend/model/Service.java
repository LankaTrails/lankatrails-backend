package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.BookingType;
import com.lankatrails.lankatrails_backend.model.enums.PriceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long serviceId;

    private String serviceName;

    private String contactNo;

    private Boolean status;

    private Double price;

    private Long duration;

    @Enumerated(EnumType.STRING)
    private PriceType priceType;

    @Enumerated(EnumType.STRING)
    private BookingType bookingType;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private  Category category;

    @OneToMany(mappedBy = "service")
    private Set<TabsSection> tabs=new HashSet<>();


    @ManyToMany
    @JoinTable(
          name = "service_policy",
          joinColumns = @JoinColumn(name = "service_id"),
          inverseJoinColumns = @JoinColumn(name = "policy_id")

    )
    private Set<PolicySection> policies=new HashSet<>();

    @ManyToMany
    @JoinTable(
          name = "service_location",
          joinColumns = @JoinColumn(name = "service_id"),
          inverseJoinColumns = @JoinColumn(name = "location_id")
    )
    private Set<Location> locations = new HashSet<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "service")
    private Set<TripItem> tripItems = new HashSet<>();

    @ManyToMany(mappedBy = "favouriteServices")
    private Set<Tourist> tourists = new HashSet<>();

    @OneToMany(mappedBy = "service")
    private List<AvailableTime> availableTimes = new ArrayList<>();

//    @OneToMany(mappedBy = "service")
//    private List<Booking> serviceBookings = new ArrayList<>();

//    @OneToMany(mappedBy = "service")
//    private List<ChatRoom> chatRooms = new ArrayList<>();

}
