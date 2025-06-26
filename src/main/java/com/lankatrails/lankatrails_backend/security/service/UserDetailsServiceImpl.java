package com.lankatrails.lankatrails_backend.security.service;

import com.lankatrails.lankatrails_backend.repositories.UserRepository;
import jakarta.persistence.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetailsImpl loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
    }
}
