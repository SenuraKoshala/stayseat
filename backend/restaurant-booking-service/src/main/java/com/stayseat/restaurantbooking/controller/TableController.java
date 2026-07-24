package com.stayseat.restaurantbooking.controller;

import com.stayseat.restaurantbooking.dto.ApiResponse;
import com.stayseat.restaurantbooking.dto.TableDtos.TableRequest;
import com.stayseat.restaurantbooking.dto.TableDtos.TableResponse;
import com.stayseat.restaurantbooking.service.TableService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurant")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping("/properties/{propertyId}/tables")
    public ApiResponse<List<TableResponse>> listByProperty(@PathVariable UUID propertyId) {
        return ApiResponse.of(tableService.listByProperty(propertyId));
    }

    @PostMapping("/tables")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ApiResponse<TableResponse> create(@Valid @RequestBody TableRequest request) {
        return ApiResponse.of(tableService.create(request));
    }

    @GetMapping("/availability")
    public ApiResponse<List<TableResponse>> availability(
            @RequestParam UUID propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String timeSlot,
            @RequestParam(defaultValue = "1") int partySize) {
        return ApiResponse.of(tableService.searchAvailability(propertyId, date, timeSlot, partySize));
    }
}
