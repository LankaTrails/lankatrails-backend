package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderRepository extends JpaRepository<Provider,Long> {
}
