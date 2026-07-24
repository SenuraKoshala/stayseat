package com.stayseat.restaurantbooking.repository;

import com.stayseat.restaurantbooking.entity.RestaurantProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RestaurantPropertyRepository extends JpaRepository<RestaurantProperty, UUID> {
    List<RestaurantProperty> findByCityIgnoreCase(String city);
}
