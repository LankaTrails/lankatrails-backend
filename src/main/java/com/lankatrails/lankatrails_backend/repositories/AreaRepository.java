package com.lankatrails.lankatrails_backend.repositories;

import com.lankatrails.lankatrails_backend.model.GuidingArea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<GuidingArea,Long> {
}
