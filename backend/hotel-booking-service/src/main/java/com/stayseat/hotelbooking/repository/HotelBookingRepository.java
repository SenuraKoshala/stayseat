package com.stayseat.hotelbooking.repository;

import com.stayseat.hotelbooking.entity.HotelBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HotelBookingRepository extends JpaRepository<HotelBooking, UUID> {
    Page<HotelBooking> findByCustomerId(UUID customerId, Pageable pageable);
}
