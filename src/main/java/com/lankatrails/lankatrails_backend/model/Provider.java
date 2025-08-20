package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

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

    @Column(name = "business_description")
    private String businessDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", columnDefinition = "VARCHAR(20)")
    private BusinessType businessType;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "location_id")
    private Location location;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "br_number")
    private String businessRegistrationNumber;

    @Column(name = "br_url")
    private String businessRegistrationUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "accommodation_approval_status", columnDefinition = "VARCHAR(20)")
    private ApprovalStatus accommodationApprovalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "tour_guide_approval_status", columnDefinition = "VARCHAR(20)")
    private ApprovalStatus tourGuideApprovalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_approval_status", columnDefinition = "VARCHAR(20)")
    private ApprovalStatus transportApprovalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_approval_status", columnDefinition = "VARCHAR(20)")
    private ApprovalStatus activityApprovalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_approval_status", columnDefinition = "VARCHAR(20)")
    private ApprovalStatus foodApprovalStatus;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_person_id", referencedColumnName = "contact_person_id")
    private ContactPerson contactPerson;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<License> licenses = new HashSet<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
    private Set<Service> services = new HashSet<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<DirectChatRoom> directChatRooms = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        super.setRole(UserRole.ROLE_PROVIDER);
        super.setStatus(UserStatus.PENDING);
    }

    public void addLicense(License license) {
        if (license != null) {
            license.setProvider(this);
            this.licenses.add(license);
        }
    }

    public void removeLicense(License license) {
        if (license != null) {
            license.setProvider(null);
            this.licenses.remove(license);
        }
    }

    public void addService(Service service) {
        if (service != null) {
            service.setProvider(this);
            this.services.add(service);
        }
    }

    public void removeService(Service service) {
        if (service != null) {
            service.setProvider(null);
            this.services.remove(service);
        }
    }

}