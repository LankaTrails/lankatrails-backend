package com.lankatrails.lankatrails_backend.security.utils;

import com.lankatrails.lankatrails_backend.exception.UserNotFoundException;
import com.lankatrails.lankatrails_backend.model.User;
import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {
    @Autowired
    UserRepository userRepository;

    public String loggedInEmail(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User Not Found with username: " + authentication.getName()));

        return user.getEmail();
    }

    public Long loggedInUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User Not Found with username: " + authentication.getName()));

        return user.getUserId();
    }

    public User loggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User Not Found with username: " + authentication.getName()));

    }
}
