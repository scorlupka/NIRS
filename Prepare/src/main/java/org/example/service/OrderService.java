package org.example.service;

import org.example.model.Order;
import org.example.model.Price;
import org.example.model.Room;
import org.example.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RoomService roomService;

    public OrderService(OrderRepository orderRepository, RoomService roomService) {
        this.orderRepository = orderRepository;
        this.roomService = roomService;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByClient(String passportNumber) {
        return orderRepository.findByClientPassportNumber(passportNumber);
    }

    public Optional<Order> findById(Integer orderNumber) {
        return orderRepository.findById(orderNumber);
    }

    public Order createOrder(Order order) {
        validateOrder(order);

        // Проверяем, что номер существует
        Room room = roomService.findById(order.getRoomNumber())
                .orElseThrow(() -> new RuntimeException("Номер не найден"));

        // Проверяем вместимость
        if (order.getGuestsCount() > room.getMaxGuests()) {
            throw new RuntimeException("Гостей больше, чем позволяет номер");
        }

        // Проверяем пересечение по датам
        LocalDate from = order.getCheckInDate();
        LocalDate to = order.getCheckOutDate();
        List<Order> conflicts = orderRepository
                .findByRoomNumberAndCheckOutDateAfterAndCheckInDateBefore(room.getRoomNumber(), from, to);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Номер занят в выбранные даты");
        }

        // Рассчитываем стоимость
        long nights = ChronoUnit.DAYS.between(from, to);
        if (nights <= 0) {
            throw new RuntimeException("Дата выезда должна быть позже даты заезда");
        }

        Price price = roomService.findPriceForRoom(room.getRoomNumber())
                .orElseThrow(() -> new RuntimeException("Для номера не задана цена"));

        int totalCost = (int) (nights * price.getBasePrice());
        order.setTotalCost(totalCost);
        order.setPaymentStatus("UNPAID");
        order.setRoomClass(room.getRoomClass());

        return orderRepository.save(order);
    }

    public Order markPaid(Integer orderNumber) {
        Order order = orderRepository.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        order.setPaymentStatus("PAID");
        return orderRepository.save(order);
    }

    private void validateOrder(Order order) {
        if (order.getClientPassportNumber() == null ||
                order.getCheckInDate() == null ||
                order.getCheckOutDate() == null ||
                order.getRoomNumber() == null ||
                order.getGuestsCount() == null) {
            throw new RuntimeException("Заполните все обязательные поля заказа");
        }
    }
}


