package com.stayseat.hotelbooking.controller;

import com.stayseat.hotelbooking.dto.ApiResponse;
import com.stayseat.hotelbooking.dto.RoomDtos.RoomRequest;
import com.stayseat.hotelbooking.dto.RoomDtos.RoomResponse;
import com.stayseat.hotelbooking.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hotel")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/properties/{propertyId}/rooms")
    public ApiResponse<List<RoomResponse>> listByProperty(@PathVariable UUID propertyId) {
        return ApiResponse.of(roomService.listByProperty(propertyId));
    }

    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HOTEL_ADMIN')")
    public ApiResponse<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
        return ApiResponse.of(roomService.create(request));
    }

    @GetMapping("/availability")
    public ApiResponse<List<RoomResponse>> availability(
            @RequestParam UUID propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return ApiResponse.of(roomService.searchAvailability(propertyId, checkIn, checkOut));
    }
}
