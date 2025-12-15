package org.example.service;

import org.example.model.Order;
import org.example.model.Price;
import org.example.model.Room;
import org.example.repository.OrderRepository;
import org.example.repository.OrderServiceLinkRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RoomService roomService;
    private final UserRepository userRepository;
    private final OrderServiceLinkRepository orderServiceLinkRepository;

    public OrderService(OrderRepository orderRepository, RoomService roomService, UserRepository userRepository,
                        OrderServiceLinkRepository orderServiceLinkRepository) {
        this.orderRepository = orderRepository;
        this.roomService = roomService;
        this.userRepository = userRepository;
        this.orderServiceLinkRepository = orderServiceLinkRepository;
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

        // Проверяем, что клиент существует
        if (!userRepository.existsById(order.getClientPassportNumber())) {
            throw new RuntimeException("Клиент с таким паспортом не найден. Пожалуйста, убедитесь, что вы зарегистрированы.");
        }

        // Если roomNumber = 0 или null, это заказ только услуги
        if (order.getRoomNumber() == null || order.getRoomNumber() == 0) {
            // Заказ только услуги - не проверяем комнату
            order.setTotalCost(0); // Стоимость будет добавлена при привязке услуги
            order.setPaymentStatus("UNPAID");
            order.setRoomClass("SERVICE_ONLY");
            return orderRepository.save(order);
        }

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
        LocalDate today = LocalDate.now();
        
        // Проверяем заказы, которые пересекаются с выбранными датами
        List<Order> conflicts = orderRepository
                .findByRoomNumberAndCheckOutDateAfterAndCheckInDateBefore(room.getRoomNumber(), from, to);
        
        // Также проверяем заказы, которые начинаются сегодня или раньше и еще не закончились
        // Если сегодня = дата заезда, то комната уже занята
        List<Order> todayConflicts = orderRepository.findAll().stream()
                .filter(o -> o.getRoomNumber() != null && o.getRoomNumber().equals(room.getRoomNumber()))
                .filter(o -> {
                    LocalDate checkIn = o.getCheckInDate();
                    LocalDate checkOut = o.getCheckOutDate();
                    // Комната занята если: заезд сегодня или раньше, и выезд сегодня или позже
                    return (checkIn.isBefore(today) || checkIn.equals(today)) && 
                           (checkOut.isAfter(today) || checkOut.equals(today));
                })
                .collect(Collectors.toList());
        
        // Проверяем, что выбранная дата заезда не совпадает с сегодняшней (комната уже занята)
        if (from.equals(today)) {
            // Проверяем, нет ли активных заказов на этот номер
            if (!todayConflicts.isEmpty()) {
                throw new RuntimeException("Номер занят в выбранные даты");
            }
        }
        
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

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public Order updateOrder(Integer orderNumber, Order order) {
        Order existingOrder = orderRepository.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        // Обновляем поля
        if (order.getCheckInDate() != null) {
            existingOrder.setCheckInDate(order.getCheckInDate());
        }
        if (order.getCheckOutDate() != null) {
            existingOrder.setCheckOutDate(order.getCheckOutDate());
        }
        if (order.getGuestsCount() != null) {
            existingOrder.setGuestsCount(order.getGuestsCount());
        }
        if (order.getRoomNumber() != null) {
            existingOrder.setRoomNumber(order.getRoomNumber());
        }
        if (order.getPaymentStatus() != null) {
            existingOrder.setPaymentStatus(order.getPaymentStatus());
        }
        
        // Пересчитываем стоимость
        Room room = roomService.findById(existingOrder.getRoomNumber())
                .orElseThrow(() -> new RuntimeException("Номер не найден"));
        
        long nights = ChronoUnit.DAYS.between(existingOrder.getCheckInDate(), existingOrder.getCheckOutDate());
        Price price = roomService.findPriceForRoom(room.getRoomNumber())
                .orElseThrow(() -> new RuntimeException("Для номера не задана цена"));
        
        existingOrder.setTotalCost((int) (nights * price.getBasePrice()));
        existingOrder.setRoomClass(room.getRoomClass());
        
        return orderRepository.save(existingOrder);
    }

    @Transactional
    public void deleteOrder(Integer orderNumber) {
        if (!orderRepository.existsById(orderNumber)) {
            throw new RuntimeException("Заказ не найден");
        }
        
        // Удаляем все связанные услуги перед удалением заказа
        orderServiceLinkRepository.deleteByOrderNumber(orderNumber);
        
        // Удаляем заказ
        orderRepository.deleteById(orderNumber);
    }

    private void validateOrder(Order order) {
        if (order.getClientPassportNumber() == null ||
                order.getCheckInDate() == null ||
                order.getCheckOutDate() == null ||
                order.getGuestsCount() == null) {
            throw new RuntimeException("Заполните все обязательные поля заказа");
        }
        // roomNumber может быть null или 0 для заказа только услуги
    }
}



