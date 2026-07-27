package com.stayseat.restaurantbooking.controller;

import com.stayseat.restaurantbooking.dto.ApiResponse;
import com.stayseat.restaurantbooking.dto.PropertyDtos.PropertyRequest;
import com.stayseat.restaurantbooking.dto.PropertyDtos.PropertyResponse;
import com.stayseat.restaurantbooking.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurant/properties")
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
    @PreAuthorize("hasRole('RESTAURANT_ADMIN')")
    public ApiResponse<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        return ApiResponse.of(propertyService.create(request));
    }
}
