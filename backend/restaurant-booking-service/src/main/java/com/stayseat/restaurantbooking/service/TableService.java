package com.stayseat.restaurantbooking.service;

import com.stayseat.restaurantbooking.dto.TableDtos.TableRequest;
import com.stayseat.restaurantbooking.dto.TableDtos.TableResponse;
import com.stayseat.restaurantbooking.entity.RestaurantTable;
import com.stayseat.restaurantbooking.exception.ApiException;
import com.stayseat.restaurantbooking.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final PropertyService propertyService;

    public TableService(RestaurantTableRepository tableRepository, PropertyService propertyService) {
        this.tableRepository = tableRepository;
        this.propertyService = propertyService;
    }

    public List<TableResponse> listByProperty(UUID propertyId) {
        propertyService.getOrThrow(propertyId); // 404 if the property doesn't exist
        return tableRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public TableResponse create(TableRequest request) {
        propertyService.getOrThrow(request.propertyId());

        RestaurantTable table = RestaurantTable.builder()
                .propertyId(request.propertyId())
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .build();
        return toResponse(tableRepository.save(table));
    }

    public List<TableResponse> searchAvailability(UUID propertyId, LocalDate date, String timeSlot, int partySize) {
        propertyService.getOrThrow(propertyId);
        return tableRepository.findAvailableTables(propertyId, date, timeSlot, partySize)
                .stream().map(this::toResponse).toList();
    }

    RestaurantTable getOrThrow(UUID id) {
        return tableRepository.findById(id).orElseThrow(() -> ApiException.notFound("Table"));
    }

    private TableResponse toResponse(RestaurantTable t) {
        return new TableResponse(t.getId(), t.getPropertyId(), t.getTableNumber(), t.getCapacity());
    }
}
