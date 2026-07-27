package com.stayseat.hotelbooking.repository;

import com.stayseat.hotelbooking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByPropertyId(UUID propertyId);

    /**
     * Rooms belonging to a property that have NO PENDING/CONFIRMED booking
     * overlapping [checkIn, checkOut). This mirrors the exclusion constraint
     * in V1__init.sql - it's the fast application-layer check used to build
     * the /availability response; the DB constraint is the final guarantee
     * against a race under concurrent booking attempts.
     */
    @Query("""
            SELECT r FROM Room r
            WHERE r.propertyId = :propertyId
            AND r.id NOT IN (
                SELECT b.roomId FROM HotelBooking b
                WHERE b.status IN ('PENDING', 'CONFIRMED')
                AND b.checkInDate < :checkOut
                AND b.checkOutDate > :checkIn
            )
            """)
    List<Room> findAvailableRooms(
            @Param("propertyId") UUID propertyId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
