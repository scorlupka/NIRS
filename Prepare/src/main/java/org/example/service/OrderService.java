package org.example.service;

import org.example.model.Order;
import org.example.model.Room;
import org.example.model.RoomPrice;
import org.example.model.ServicePrice;
import org.example.repository.OrderRepository;
import org.example.repository.OrderServiceLinkRepository;
import org.example.repository.ServicePriceRepository;
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
    private final ServicePriceRepository servicePriceRepository;

    public OrderService(OrderRepository orderRepository, RoomService roomService, UserRepository userRepository,
                        OrderServiceLinkRepository orderServiceLinkRepository,
                        ServicePriceRepository servicePriceRepository) {
        this.orderRepository = orderRepository;
        this.roomService = roomService;
        this.userRepository = userRepository;
        this.orderServiceLinkRepository = orderServiceLinkRepository;
        this.servicePriceRepository = servicePriceRepository;
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
            order.setPaymentStatus("UNPAID");
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

        order.setPaymentStatus("UNPAID");

        Order savedOrder = orderRepository.save(order);
        
        // Обновляем статус комнаты после создания заказа
        roomService.updateRoomStatusByNumber(room.getRoomNumber());
        
        return savedOrder;
    }

    public Order markPaid(Integer orderNumber) {
        Order order = orderRepository.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        order.setPaymentStatus("PAID");
        return orderRepository.save(order);
    }

    @Transactional
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
        
        Order savedOrder = orderRepository.save(existingOrder);
        
        // Обновляем статус комнаты после обновления заказа (если есть номер)
        if (existingOrder.getRoomNumber() != null && existingOrder.getRoomNumber() != 0) {
            roomService.updateRoomStatusByNumber(existingOrder.getRoomNumber());
        }
        
        return savedOrder;
    }

    @Transactional
    public void deleteOrder(Integer orderNumber) {
        if (!orderRepository.existsById(orderNumber)) {
            throw new RuntimeException("Заказ не найден");
        }
        
        // Получаем номер комнаты перед удалением для обновления статуса
        Order order = orderRepository.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        Integer roomNumber = order.getRoomNumber();
        
        // Удаляем все связанные услуги перед удалением заказа
        orderServiceLinkRepository.deleteByOrderNumber(orderNumber);
        
        // Удаляем заказ
        orderRepository.deleteById(orderNumber);
        
        // Обновляем статус комнаты после удаления заказа
        if (roomNumber != null && roomNumber != 0) {
            roomService.updateRoomStatusByNumber(roomNumber);
        }
    }

    /**
     * Вычисляет общую стоимость заказа динамически
     * Включает стоимость номера (если есть) и стоимость всех услуг
     */
    public int calculateOrderCost(Order order) {
        if (order == null) {
            return 0;
        }
        
        int cost = 0;
        
        // Стоимость номера
        if (order.getRoomNumber() != null && order.getRoomNumber() != 0 
                && order.getCheckInDate() != null && order.getCheckOutDate() != null) {
            Room room = roomService.findById(order.getRoomNumber()).orElse(null);
            if (room != null) {
                Optional<RoomPrice> priceOpt = roomService.findPriceForRoom(room.getRoomNumber());
                if (priceOpt.isPresent()) {
                    try {
                        long nights = ChronoUnit.DAYS.between(order.getCheckInDate(), order.getCheckOutDate());
                        if (nights > 0) {
                            cost += (int) (nights * priceOpt.get().getBasePrice());
                        }
                    } catch (Exception e) {
                        // Игнорируем ошибки вычисления дат
                    }
                }
            }
        }
        
        // Стоимость услуг
        if (order.getOrderNumber() != null) {
            try {
                List<Long> serviceIds = orderServiceLinkRepository.findServiceIdsByOrderNumber(order.getOrderNumber());
                for (Long serviceId : serviceIds) {
                    Optional<ServicePrice> servicePriceOpt = servicePriceRepository.findByServiceId(serviceId);
                    if (servicePriceOpt.isPresent()) {
                        cost += servicePriceOpt.get().getBasePrice();
                    }
                }
            } catch (Exception e) {
                // Игнорируем ошибки получения услуг
            }
        }
        
        return cost;
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



