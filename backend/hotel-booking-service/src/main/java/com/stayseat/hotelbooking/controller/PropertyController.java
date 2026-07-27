package com.stayseat.hotelbooking.controller;

import com.stayseat.hotelbooking.dto.ApiResponse;
import com.stayseat.hotelbooking.dto.PropertyDtos.PropertyRequest;
import com.stayseat.hotelbooking.dto.PropertyDtos.PropertyResponse;
import com.stayseat.hotelbooking.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hotel/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public ApiResponse<List<PropertyResponse>> list(@RequestParam(required = false) String city) {
        return ApiResponse.of(propertyService.list(city));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HOTEL_ADMIN')")
    public ApiResponse<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        return ApiResponse.of(propertyService.create(request));
    }
}
