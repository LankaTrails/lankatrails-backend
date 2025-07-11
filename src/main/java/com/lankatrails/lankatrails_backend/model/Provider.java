package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "providers")
@Getter
@Setter
@NoArgsConstructor
public class Provider extends User {

     @Size(max = 50)
     @Column(name = "business_name")
     private String businessName;

     @Size(max = 100)
     @Column(name = "business_description")
     private String businessDescription;

     @Enumerated(EnumType.STRING)
     @Column(name = "business_type")
     private BusinessType businessType;

     @Column(name = "cover_image_url")
     private String coverImageUrl;

     @Column(name = "br_number")
     private String businessRegistrationNumber;

     @Column(name = "br_url")
     private String businessRegistrationUrl;

     @Column(name = "accommodation_approval_status")
     private ApprovalStatus accommodationApprovalStatus = ApprovalStatus.NOT_REQUSTED;

     @Column(name = "tour_guide_approval_status")
     private ApprovalStatus tourGuideApprovalStatus = ApprovalStatus.NOT_REQUSTED;

     @Column(name = "transport_approval_status")
     private ApprovalStatus transportApprovalStatus = ApprovalStatus.NOT_REQUSTED;

     @Column(name = "activity_approval_status")
     private ApprovalStatus activityApprovalStatus = ApprovalStatus.NOT_REQUSTED;

     @Column(name = "food_approval_status")
     private ApprovalStatus foodApprovalStatus = ApprovalStatus.NOT_REQUSTED;

     @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     @JoinColumn(name = "contact_person_id", referencedColumnName = "contact_person_id")
     private ContactPerson contactPerson;

     @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     private Set<License> licenses = new HashSet<>();

     @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
     private Set<Services> services = new HashSet<>();

     @PrePersist
     protected void onCreate() {
          super.setRole(UserRole.ROLE_PROVIDER);
          super.setStatus(UserStatus.PENDING);
     }

}