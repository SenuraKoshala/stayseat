package com.stayseat.restaurantbooking.service;

import com.stayseat.restaurantbooking.dto.PropertyDtos.PropertyRequest;
import com.stayseat.restaurantbooking.dto.PropertyDtos.PropertyResponse;
import com.stayseat.restaurantbooking.entity.RestaurantProperty;
import com.stayseat.restaurantbooking.exception.ApiException;
import com.stayseat.restaurantbooking.repository.RestaurantPropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PropertyService {

    private final RestaurantPropertyRepository propertyRepository;

    public PropertyService(RestaurantPropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<PropertyResponse> list(String city) {
        List<RestaurantProperty> properties = (city == null || city.isBlank())
                ? propertyRepository.findAll()
                : propertyRepository.findByCityIgnoreCase(city);
        return properties.stream().map(this::toResponse).toList();
    }

    @Transactional
    public PropertyResponse create(PropertyRequest request) {
        RestaurantProperty property = RestaurantProperty.builder()
                .name(request.name())
                .city(request.city())
                .address(request.address())
                .description(request.description())
                .build();
        return toResponse(propertyRepository.save(property));
    }

    RestaurantProperty getOrThrow(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Property"));
    }

    private PropertyResponse toResponse(RestaurantProperty p) {
        return new PropertyResponse(p.getId(), p.getName(), p.getCity(), p.getAddress(),
                p.getDescription(), p.getCreatedAt());
    }
}
