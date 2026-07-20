package com.stayseat.hotelbooking.service;

import com.stayseat.hotelbooking.dto.PropertyDtos.PropertyRequest;
import com.stayseat.hotelbooking.dto.PropertyDtos.PropertyResponse;
import com.stayseat.hotelbooking.entity.Property;
import com.stayseat.hotelbooking.exception.ApiException;
import com.stayseat.hotelbooking.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<PropertyResponse> list(String city) {
        List<Property> properties = (city == null || city.isBlank())
                ? propertyRepository.findAll()
                : propertyRepository.findByCityIgnoreCase(city);
        return properties.stream().map(this::toResponse).toList();
    }

    @Transactional
    public PropertyResponse create(PropertyRequest request) {
        Property property = Property.builder()
                .name(request.name())
                .city(request.city())
                .address(request.address())
                .description(request.description())
                .build();
        return toResponse(propertyRepository.save(property));
    }

    Property getOrThrow(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Property"));
    }

    private PropertyResponse toResponse(Property p) {
        return new PropertyResponse(p.getId(), p.getName(), p.getCity(), p.getAddress(),
                p.getDescription(), p.getCreatedAt());
    }
}
