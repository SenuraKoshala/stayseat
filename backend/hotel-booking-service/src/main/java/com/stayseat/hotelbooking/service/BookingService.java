package com.stayseat.hotelbooking.service;

import com.stayseat.hotelbooking.config.CurrentUser;
import com.stayseat.hotelbooking.dto.BookingDtos.BookingResponse;
import com.stayseat.hotelbooking.dto.BookingDtos.CreateBookingRequest;
import com.stayseat.hotelbooking.dto.Money;
import com.stayseat.hotelbooking.entity.BookingStatus;
import com.stayseat.hotelbooking.entity.HotelBooking;
import com.stayseat.hotelbooking.entity.Room;
import com.stayseat.hotelbooking.event.DomainEvent;
import com.stayseat.hotelbooking.event.EventPublisher;
import com.stayseat.hotelbooking.exception.ApiException;
import com.stayseat.hotelbooking.repository.HotelBookingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingService {

    private final HotelBookingRepository bookingRepository;
    private final RoomService roomService;
    private final EventPublisher eventPublisher;

    public BookingService(HotelBookingRepository bookingRepository, RoomService roomService,
                           EventPublisher eventPublisher) {
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BookingResponse create(CreateBookingRequest request, CurrentUser customer) {
        if (!request.checkOutDate().isAfter(request.checkInDate())) {
            throw ApiException.invalidDateRange();
        }

        Room room = roomService.getOrThrow(request.roomId());

        long nights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        HotelBooking booking = HotelBooking.builder()
                .roomId(room.getId())
                .customerId(customer.userId())
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .status(BookingStatus.PENDING)
                .totalAmount(totalAmount)
                .currency(room.getCurrency())
                .build();

        // saveAndFlush (not just save) so the Postgres EXCLUDE constraint from
        // V1__init.sql fires here, inside this try, rather than silently at
        // end-of-transaction - GlobalExceptionHandler turns the resulting
        // DataIntegrityViolationException into a 409 ROOM_NOT_AVAILABLE.
        HotelBooking saved = bookingRepository.saveAndFlush(booking);

        eventPublisher.publish(DomainEvent.of("HotelBookingCreated", Map.of(
                "bookingId", saved.getId(),
                "customerId", saved.getCustomerId(),
                "roomId", saved.getRoomId(),
                "checkInDate", saved.getCheckInDate(),
                "checkOutDate", saved.getCheckOutDate(),
                "totalAmount", saved.getTotalAmount()
        )));

        return toResponse(saved);
    }

    public BookingResponse getById(UUID id, CurrentUser requester) {
        HotelBooking booking = getOrThrow(id);
        requireOwnerOrAdmin(booking, requester);
        return toResponse(booking);
    }

    public Page<BookingResponse> listMine(CurrentUser customer, Pageable pageable) {
        return bookingRepository.findByCustomerId(customer.userId(), pageable).map(this::toResponse);
    }

    @Transactional
    public BookingResponse confirm(UUID id) {
        HotelBooking booking = getOrThrow(id);
        booking.setStatus(BookingStatus.CONFIRMED);
        HotelBooking saved = bookingRepository.save(booking);

        eventPublisher.publish(DomainEvent.of("HotelBookingConfirmed", Map.of(
                "bookingId", saved.getId(),
                "customerId", saved.getCustomerId(),
                "checkInDate", saved.getCheckInDate(),
                "checkOutDate", saved.getCheckOutDate()
        )));

        return toResponse(saved);
    }

    @Transactional
    public BookingResponse cancel(UUID id, String reason, CurrentUser requester) {
        HotelBooking booking = getOrThrow(id);
        requireOwnerOrAdmin(booking, requester);

        booking.setStatus(BookingStatus.CANCELLED);
        HotelBooking saved = bookingRepository.save(booking);

        eventPublisher.publish(DomainEvent.of("HotelBookingCancelled", Map.of(
                "bookingId", saved.getId(),
                "customerId", saved.getCustomerId(),
                "reason", reason == null ? "" : reason
        )));

        return toResponse(saved);
    }

    private void requireOwnerOrAdmin(HotelBooking booking, CurrentUser requester) {
        boolean isOwner = booking.getCustomerId().equals(requester.userId());
        boolean isAdmin = requester.hasRole("HOTEL_ADMIN") || requester.hasRole("SYSTEM_ADMIN");
        if (!isOwner && !isAdmin) {
            throw ApiException.forbidden("You do not have access to this booking.");
        }
    }

    private HotelBooking getOrThrow(UUID id) {
        return bookingRepository.findById(id).orElseThrow(() -> ApiException.notFound("Booking"));
    }

    private BookingResponse toResponse(HotelBooking b) {
        return new BookingResponse(b.getId(), b.getRoomId(), b.getCustomerId(), b.getCheckInDate(),
                b.getCheckOutDate(), b.getStatus(), new Money(b.getTotalAmount(), b.getCurrency()),
                b.getCreatedAt());
    }
}
