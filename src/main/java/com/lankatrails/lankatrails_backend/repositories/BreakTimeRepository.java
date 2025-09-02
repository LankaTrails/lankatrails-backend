package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.BreakTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BreakTimeRepository extends JpaRepository<BreakTime, Long> {
}
