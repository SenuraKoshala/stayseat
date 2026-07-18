package com.stayseat.hotelbooking.repository;

import com.stayseat.hotelbooking.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    List<Property> findByCityIgnoreCase(String city);
}
