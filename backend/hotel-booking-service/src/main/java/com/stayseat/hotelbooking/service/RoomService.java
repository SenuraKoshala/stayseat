package com.stayseat.hotelbooking.service;

import com.stayseat.hotelbooking.dto.Money;
import com.stayseat.hotelbooking.dto.RoomDtos.RoomRequest;
import com.stayseat.hotelbooking.dto.RoomDtos.RoomResponse;
import com.stayseat.hotelbooking.entity.Room;
import com.stayseat.hotelbooking.exception.ApiException;
import com.stayseat.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final PropertyService propertyService;

    public RoomService(RoomRepository roomRepository, PropertyService propertyService) {
        this.roomRepository = roomRepository;
        this.propertyService = propertyService;
    }

    public List<RoomResponse> listByProperty(UUID propertyId) {
        propertyService.getOrThrow(propertyId); // 404 if the property doesn't exist
        return roomRepository.findByPropertyId(propertyId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        propertyService.getOrThrow(request.propertyId());

        Room room = Room.builder()
                .propertyId(request.propertyId())
                .roomNumber(request.roomNumber())
                .type(request.type())
                .capacity(request.capacity())
                .pricePerNight(request.pricePerNight())
                .currency(request.currency() == null ? "LKR" : request.currency())
                .build();
        return toResponse(roomRepository.save(room));
    }

    public List<RoomResponse> searchAvailability(UUID propertyId, LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw ApiException.invalidDateRange();
        }
        propertyService.getOrThrow(propertyId);
        return roomRepository.findAvailableRooms(propertyId, checkIn, checkOut)
                .stream().map(this::toResponse).toList();
    }

    Room getOrThrow(UUID id) {
        return roomRepository.findById(id).orElseThrow(() -> ApiException.notFound("Room"));
    }

    private RoomResponse toResponse(Room r) {
        return new RoomResponse(r.getId(), r.getPropertyId(), r.getRoomNumber(), r.getType(),
                r.getCapacity(), new Money(r.getPricePerNight(), r.getCurrency()));
    }
}
