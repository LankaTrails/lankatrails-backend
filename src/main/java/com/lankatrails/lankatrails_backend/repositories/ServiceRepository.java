package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service,Integer> {


}
