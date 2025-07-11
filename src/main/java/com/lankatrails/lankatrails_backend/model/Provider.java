package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.ServiceCategory;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
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

     @Column(name = "contact_number")
     private String contactNumber;

     @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL)
     private Set<Services> services = new HashSet<>();

     @OneToMany(mappedBy = "provider" , cascade = CascadeType.ALL)
     private Set<PolicySection> policies = new HashSet<>();

     @PrePersist
     protected void onCreate() {
          super.setRole(UserRole.ROLE_PROVIDER);
          super.setStatus(UserStatus.PENDING);
     }


}