package com.stayseat.restaurantbooking.repository;

import com.stayseat.restaurantbooking.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, UUID> {

    List<RestaurantTable> findByPropertyId(UUID propertyId);

    /**
     * Tables belonging to a property that seat at least partySize guests and
     * have NO PENDING/CONFIRMED booking on the given date + time slot. This
     * mirrors the partial unique index in V1__init.sql - it's the fast
     * application-layer check used to build the /availability response; the DB
     * index is the final guarantee against a race under concurrent booking
     * attempts.
     */
    @Query("""
            SELECT t FROM RestaurantTable t
            WHERE t.propertyId = :propertyId
            AND t.capacity >= :partySize
            AND t.id NOT IN (
                SELECT b.tableId FROM RestaurantBooking b
                WHERE b.status IN ('PENDING', 'CONFIRMED')
                AND b.reservationDate = :date
                AND b.timeSlot = :timeSlot
            )
            """)
    List<RestaurantTable> findAvailableTables(
            @Param("propertyId") UUID propertyId,
            @Param("date") LocalDate date,
            @Param("timeSlot") String timeSlot,
            @Param("partySize") int partySize
    );
}
