package org.example.service;

import org.example.model.Price;
import org.example.model.Room;
import org.example.repository.PriceRepository;
import org.example.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final PriceRepository priceRepository;

    public RoomService(RoomRepository roomRepository, PriceRepository priceRepository) {
        this.roomRepository = roomRepository;
        this.priceRepository = priceRepository;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Optional<Room> findById(Integer roomNumber) {
        return roomRepository.findById(roomNumber);
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void delete(Integer roomNumber) {
        roomRepository.deleteById(roomNumber);
    }

    public Optional<Price> findPriceForRoom(Integer roomNumber) {
        return priceRepository.findFirstByObjectTypeAndObjectNumber("ROOM", roomNumber);
    }

    public Price saveRoomPrice(Integer roomNumber, Integer basePrice) {
        Price price = priceRepository.findFirstByObjectTypeAndObjectNumber("ROOM", roomNumber)
                .orElseGet(Price::new);
        price.setObjectType("ROOM");
        price.setObjectNumber(roomNumber);
        price.setBasePrice(basePrice);
        return priceRepository.save(price);
    }
}




