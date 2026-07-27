package com.stayseat.hotelbooking.controller;

import com.stayseat.hotelbooking.config.AuthUtil;
import com.stayseat.hotelbooking.dto.ApiResponse;
import com.stayseat.hotelbooking.dto.BookingDtos.BookingResponse;
import com.stayseat.hotelbooking.dto.BookingDtos.CancelBookingRequest;
import com.stayseat.hotelbooking.dto.BookingDtos.CreateBookingRequest;
import com.stayseat.hotelbooking.dto.PageMeta;
import com.stayseat.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hotel/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        return ApiResponse.of(bookingService.create(request, AuthUtil.currentUser()));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(bookingService.getById(id, AuthUtil.currentUser()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> listMine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BookingResponse> result = bookingService.listMine(AuthUtil.currentUser(), pageable);
        PageMeta meta = new PageMeta(result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
        return ApiResponse.of(result.getContent(), meta);
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('HOTEL_ADMIN')")
    public ApiResponse<BookingResponse> confirm(@PathVariable UUID id) {
        return ApiResponse.of(bookingService.confirm(id));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<BookingResponse> cancel(@PathVariable UUID id,
                                                @RequestBody(required = false) CancelBookingRequest request) {
        String reason = request == null ? null : request.reason();
        return ApiResponse.of(bookingService.cancel(id, reason, AuthUtil.currentUser()));
    }
}
