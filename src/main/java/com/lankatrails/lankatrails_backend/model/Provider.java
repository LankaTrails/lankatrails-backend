package com.lankatrails.lankatrails_backend.model;

import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.model.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

     @PrePersist
     protected void onCreate() {
        // Assuming there's a method in User to set the role
        super.setRole(UserRole.PROVIDER);
        super.setStatus(UserStatus.ACTIVE);
     }

}
