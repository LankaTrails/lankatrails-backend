package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.ProviderCategory;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

     @Size(max = 255)
     @Column(name = "logo_url")
     private String logoUrl;

     @ElementCollection(targetClass = ProviderCategory.class)
     @CollectionTable(name = "provider_categories", joinColumns = @JoinColumn(name = "user_id"))
     @Column(name = "category")
     @Enumerated(EnumType.STRING)
     private Set<ProviderCategory> categories;

     @PrePersist
     protected void onCreate() {
          super.setRole(UserRole.PROVIDER);
          super.setStatus(UserStatus.PENDING);
     }
}