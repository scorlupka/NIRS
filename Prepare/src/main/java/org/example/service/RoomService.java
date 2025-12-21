package org.example.service;

import org.example.model.Order;
import org.example.model.Price;
import org.example.model.Room;
import org.example.repository.OrderRepository;
import org.example.repository.PriceRepository;
import org.example.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final PriceRepository priceRepository;
    private final OrderRepository orderRepository;

    public RoomService(RoomRepository roomRepository, PriceRepository priceRepository, OrderRepository orderRepository) {
        this.roomRepository = roomRepository;
        this.priceRepository = priceRepository;
        this.orderRepository = orderRepository;
    }

    public List<Room> findAll() {
        List<Room> rooms = roomRepository.findAll();
        // Автоматически обновляем статусы всех комнат
        rooms.forEach(this::updateRoomStatus);
        return rooms;
    }

    public Optional<Room> findById(Integer roomNumber) {
        Optional<Room> roomOpt = roomRepository.findById(roomNumber);
        roomOpt.ifPresent(this::updateRoomStatus);
        return roomOpt;
    }

    @Transactional
    public Room save(Room room) {
        // При сохранении автоматически устанавливаем статус
        updateRoomStatus(room);
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

    /**
     * Автоматически обновляет статус комнаты на основе активных заказов
     * Статус "OCCUPIED" если есть активный заказ, иначе "FREE"
     */
    @Transactional
    public void updateRoomStatus(Room room) {
        if (room == null || room.getRoomNumber() == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        List<Order> orders = orderRepository.findByRoomNumber(room.getRoomNumber());

        // Проверяем, есть ли активный заказ (дата заезда <= сегодня <= дата выезда)
        boolean isOccupied = orders.stream()
                .filter(order -> order.getCheckInDate() != null && order.getCheckOutDate() != null)
                .anyMatch(order -> {
                    LocalDate checkIn = order.getCheckInDate();
                    LocalDate checkOut = order.getCheckOutDate();
                    // Комната занята если: заезд сегодня или раньше, и выезд сегодня или позже
                    return (checkIn.isBefore(today) || checkIn.equals(today)) &&
                           (checkOut.isAfter(today) || checkOut.equals(today));
                });

        // Устанавливаем статус
        room.setRoomStatus(isOccupied ? "OCCUPIED" : "FREE");
    }

    /**
     * Обновляет статус комнаты по номеру
     */
    @Transactional
    public void updateRoomStatusByNumber(Integer roomNumber) {
        roomRepository.findById(roomNumber).ifPresent(this::updateRoomStatus);
    }
}






