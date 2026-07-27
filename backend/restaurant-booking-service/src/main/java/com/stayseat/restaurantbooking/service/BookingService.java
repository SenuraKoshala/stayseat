package com.stayseat.restaurantbooking.service;

import com.stayseat.restaurantbooking.config.CurrentUser;
import com.stayseat.restaurantbooking.dto.BookingDtos.BookingResponse;
import com.stayseat.restaurantbooking.dto.BookingDtos.CreateBookingRequest;
import com.stayseat.restaurantbooking.entity.BookingStatus;
import com.stayseat.restaurantbooking.entity.RestaurantBooking;
import com.stayseat.restaurantbooking.entity.RestaurantTable;
import com.stayseat.restaurantbooking.event.DomainEvent;
import com.stayseat.restaurantbooking.event.EventPublisher;
import com.stayseat.restaurantbooking.exception.ApiException;
import com.stayseat.restaurantbooking.repository.RestaurantBookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {

    private final RestaurantBookingRepository bookingRepository;
    private final TableService tableService;
    private final EventPublisher eventPublisher;

    public BookingService(RestaurantBookingRepository bookingRepository, TableService tableService,
                          EventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.tableService = tableService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BookingResponse create(CreateBookingRequest request, CurrentUser customer) {
        RestaurantTable table = tableService.getOrThrow(request.tableId());

        if (request.partySize() > table.getCapacity()) {
            throw ApiException.tableNotAvailable();
        }

        RestaurantBooking booking = RestaurantBooking.builder()
                .tableId(table.getId())
                .customerId(customer.userId())
                .reservationDate(request.reservationDate())
                .timeSlot(request.timeSlot())
                .partySize(request.partySize())
                .status(BookingStatus.PENDING)
                .build();

        // saveAndFlush (not just save) so the Postgres partial unique index from
        // V1__init.sql fires here, inside this call, rather than silently at
        // end-of-transaction - GlobalExceptionHandler turns the resulting
        // DataIntegrityViolationException into a 409 TABLE_NOT_AVAILABLE.
        RestaurantBooking saved = bookingRepository.saveAndFlush(booking);

        eventPublisher.publish(DomainEvent.of("RestaurantBookingCreated", Map.of(
                "bookingId", saved.getId(),
                "customerId", saved.getCustomerId(),
                "tableId", saved.getTableId(),
                "reservationDate", saved.getReservationDate(),
                "timeSlot", saved.getTimeSlot(),
                "partySize", saved.getPartySize()
        )));

        return toResponse(saved);
    }

    public BookingResponse getById(UUID id, CurrentUser requester) {
        RestaurantBooking booking = getOrThrow(id);
        requireOwnerOrAdmin(booking, requester);
        return toResponse(booking);
    }

    public Page<BookingResponse> listMine(CurrentUser customer, Pageable pageable) {
        return bookingRepository.findByCustomerId(customer.userId(), pageable).map(this::toResponse);
    }

    @Transactional
    public BookingResponse confirm(UUID id) {
        RestaurantBooking booking = getOrThrow(id);
        booking.setStatus(BookingStatus.CONFIRMED);
        RestaurantBooking saved = bookingRepository.save(booking);

        eventPublisher.publish(DomainEvent.of("RestaurantBookingConfirmed", Map.of(
                "bookingId", saved.getId(),
                "customerId", saved.getCustomerId(),
                "tableId", saved.getTableId(),
                "reservationDate", saved.getReservationDate(),
                "timeSlot", saved.getTimeSlot()
        )));

        return toResponse(saved);
    }

    @Transactional
    public BookingResponse cancel(UUID id, String reason, CurrentUser requester) {
        RestaurantBooking booking = getOrThrow(id);
        requireOwnerOrAdmin(booking, requester);

        booking.setStatus(BookingStatus.CANCELLED);
        RestaurantBooking saved = bookingRepository.save(booking);

        eventPublisher.publish(DomainEvent.of("RestaurantBookingCancelled", Map.of(
                "bookingId", saved.getId(),
                "customerId", saved.getCustomerId(),
                "reason", reason == null ? "" : reason
        )));

        return toResponse(saved);
    }

    private void requireOwnerOrAdmin(RestaurantBooking booking, CurrentUser requester) {
        boolean isOwner = booking.getCustomerId().equals(requester.userId());
        boolean isAdmin = requester.hasRole("RESTAURANT_ADMIN") || requester.hasRole("SYSTEM_ADMIN");
        if (!isOwner && !isAdmin) {
            throw ApiException.forbidden("You do not have access to this booking.");
        }
    }

    private RestaurantBooking getOrThrow(UUID id) {
        return bookingRepository.findById(id).orElseThrow(() -> ApiException.notFound("Booking"));
    }

    private BookingResponse toResponse(RestaurantBooking b) {
        return new BookingResponse(b.getId(), b.getTableId(), b.getCustomerId(), b.getReservationDate(),
                b.getTimeSlot(), b.getPartySize(), b.getStatus(), b.getCreatedAt());
    }
}
