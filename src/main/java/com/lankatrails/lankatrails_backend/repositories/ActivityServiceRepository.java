package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.ActivityService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ActivityServiceRepository extends JpaRepository<ActivityService,Long> {
       Optional<ActivityService> findByServiceName(String serviceName);

}
