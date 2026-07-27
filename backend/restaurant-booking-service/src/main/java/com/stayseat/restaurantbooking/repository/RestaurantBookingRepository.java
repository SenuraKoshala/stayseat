package com.stayseat.restaurantbooking.repository;

import com.stayseat.restaurantbooking.entity.RestaurantBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RestaurantBookingRepository extends JpaRepository<RestaurantBooking, UUID> {
    Page<RestaurantBooking> findByCustomerId(UUID customerId, Pageable pageable);
}
