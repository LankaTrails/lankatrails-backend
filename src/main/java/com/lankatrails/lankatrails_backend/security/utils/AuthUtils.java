package com.lankatrails.lankatrails_backend.security.utils;

import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.enums.UserRole;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import com.lankatrails.lankatrails_backend.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {
    @Autowired
    UserRepository userRepository;

    private UserDetailsImpl getLoggedInUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        throw new UserNotFoundException("User Not Found!");
    }

    public String loggedInEmail() {
        UserDetailsImpl userDetails = getLoggedInUserDetails();
        return (userDetails != null) ? userDetails.getEmail() : null;
    }

    public Long loggedInUserId() {
        UserDetailsImpl userDetails = getLoggedInUserDetails();
        return (userDetails != null) ? userDetails.getId() : null;
    }

    public UserRole loggedInUserRole() {
        UserDetailsImpl userDetails = getLoggedInUserDetails();
        return (userDetails != null) ? userDetails.getRole() : null;
    }

//    public User loggedInUser(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        return userRepository.findByEmail(authentication.getName())
//                .orElseThrow(() -> new UserNotFoundException("User Not Found with username: " + authentication.getName()));
//
//    }
}
