package org.example.controller;

import org.example.model.Order;
import org.example.model.Room;
import org.example.repository.AdditionalServiceRepository;
import org.example.service.OrderService;
import org.example.service.RoomService;
import org.example.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final RoomService roomService;
    private final UserService userService;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final org.example.service.AdditionalServiceLinkService linkService;

    public OrderController(OrderService orderService, RoomService roomService, UserService userService,
                           AdditionalServiceRepository additionalServiceRepository,
                           org.example.service.AdditionalServiceLinkService linkService) {
        this.orderService = orderService;
        this.roomService = roomService;
        this.userService = userService;
        this.additionalServiceRepository = additionalServiceRepository;
        this.linkService = linkService;
    }

    // Список заказов для текущего пользователя
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public String listMyOrders(Authentication authentication, Model model) {
        String username = authentication.getName();
        String passportNumber = userService.findByUsername(username)
                .map(u -> u.getPassportNumber())
                .orElse(username);
        List<Order> orders = orderService.findByClient(passportNumber);
        
        // Получаем услуги для каждого заказа
        Map<Integer, List<org.example.model.AdditionalService>> orderServices = new HashMap<>();
        Map<Long, Integer> servicePrices = new HashMap<>();
        // Получаем классы номеров для каждого заказа
        Map<Integer, String> orderRoomClasses = new HashMap<>();
        // Вычисляем стоимость для каждого заказа
        Map<Integer, Integer> orderCosts = new HashMap<>();
        
        for (Order order : orders) {
            List<org.example.model.AdditionalService> services = linkService.findServicesForOrder(order.getOrderNumber());
            orderServices.put(order.getOrderNumber(), services);
            
            // Получаем цены для всех услуг
            for (org.example.model.AdditionalService service : services) {
                if (!servicePrices.containsKey(service.getId())) {
                    int servicePrice = linkService.getServicePrice(service.getId());
                    servicePrices.put(service.getId(), servicePrice);
                }
            }
            
            // Получаем класс номера из таблицы rooms
            if (order.getRoomNumber() != null && order.getRoomNumber() != 0) {
                roomService.findById(order.getRoomNumber())
                        .ifPresent(room -> orderRoomClasses.put(order.getOrderNumber(), room.getRoomClass()));
            } else {
                orderRoomClasses.put(order.getOrderNumber(), "Только услуга");
            }
            
            // Вычисляем стоимость заказа
            int orderCost = orderService.calculateOrderCost(order);
            orderCosts.put(order.getOrderNumber(), orderCost);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("orderServices", orderServices);
        model.addAttribute("servicePrices", servicePrices);
        model.addAttribute("orderRoomClasses", orderRoomClasses);
        model.addAttribute("orderCosts", orderCosts);
        model.addAttribute("isAdmin", false);
        return "orders";
    }

    // Форма бронирования (для обычного пользователя)
    @GetMapping("/new")
    @PreAuthorize("hasRole('USER')")
    public String newOrderForm(Model model) {
        model.addAttribute("order", new Order());
        List<Room> rooms = roomService.findAll();
        model.addAttribute("rooms", rooms);
        return "order-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public String createOrder(
            @RequestParam(required = false) String checkInDateStr,
            @RequestParam(required = false) String checkOutDateStr,
            @ModelAttribute Order order,
            Authentication auth,
            Model model) {
        try {
            // Конвертируем строки дат в LocalDate
            if (checkInDateStr != null && !checkInDateStr.isEmpty()) {
                order.setCheckInDate(LocalDate.parse(checkInDateStr));
            }
            if (checkOutDateStr != null && !checkOutDateStr.isEmpty()) {
                order.setCheckOutDate(LocalDate.parse(checkOutDateStr));
            }

            // Валидация дат
            if (order.getCheckInDate() == null) {
                model.addAttribute("error", "Необходимо указать дату заезда");
                model.addAttribute("order", order);
                List<Room> rooms = roomService.findAll();
                model.addAttribute("rooms", rooms);
                return "order-form";
            }

            if (order.getCheckOutDate() == null) {
                model.addAttribute("error", "Необходимо указать дату выезда");
                model.addAttribute("order", order);
                List<Room> rooms = roomService.findAll();
                model.addAttribute("rooms", rooms);
                return "order-form";
            }

            if (order.getCheckInDate().isBefore(LocalDate.now())) {
                model.addAttribute("error", "Дата заезда не может быть раньше сегодняшней");
                model.addAttribute("order", order);
                List<Room> rooms = roomService.findAll();
                model.addAttribute("rooms", rooms);
                return "order-form";
            }

            if (order.getCheckOutDate().isBefore(order.getCheckInDate()) || order.getCheckOutDate().equals(order.getCheckInDate())) {
                model.addAttribute("error", "Дата выезда должна быть позже даты заезда");
                model.addAttribute("order", order);
                List<Room> rooms = roomService.findAll();
                model.addAttribute("rooms", rooms);
                return "order-form";
            }

            if (order.getClientPassportNumber() == null || order.getClientPassportNumber().isEmpty()) {
                String username = auth.getName();
                String passportNumber = userService.findByUsername(username)
                        .map(u -> u.getPassportNumber())
                        .orElse(username);
                order.setClientPassportNumber(passportNumber);
            }

            if (order.getRoomNumber() == null) {
                model.addAttribute("error", "Необходимо выбрать номер");
                model.addAttribute("order", order);
                List<Room> rooms = roomService.findAll();
                model.addAttribute("rooms", rooms);
                return "order-form";
            }

            orderService.createOrder(order);
            return "redirect:/orders";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("order", order);
            List<Room> rooms = roomService.findAll();
            model.addAttribute("rooms", rooms);
            return "order-form";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при создании заказа: " + e.getMessage());
            model.addAttribute("order", order);
            List<Room> rooms = roomService.findAll();
            model.addAttribute("rooms", rooms);
            return "order-form";
        }
    }

    // Список всех заказов для администратора
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public String listAllOrders(Model model) {
        List<Order> orders = orderService.findAll();
        
        // Получаем услуги для каждого заказа
        Map<Integer, List<org.example.model.AdditionalService>> orderServices = new HashMap<>();
        Map<Long, Integer> servicePrices = new HashMap<>();
        // Получаем классы номеров для каждого заказа
        Map<Integer, String> orderRoomClasses = new HashMap<>();
        // Вычисляем стоимость для каждого заказа
        Map<Integer, Integer> orderCosts = new HashMap<>();
        
        for (Order order : orders) {
            List<org.example.model.AdditionalService> services = linkService.findServicesForOrder(order.getOrderNumber());
            orderServices.put(order.getOrderNumber(), services);
            
            // Получаем цены для всех услуг
            for (org.example.model.AdditionalService service : services) {
                if (!servicePrices.containsKey(service.getId())) {
                    int servicePrice = linkService.getServicePrice(service.getId());
                    servicePrices.put(service.getId(), servicePrice);
                }
            }
            
            // Получаем класс номера из таблицы rooms
            if (order.getRoomNumber() != null && order.getRoomNumber() != 0) {
                roomService.findById(order.getRoomNumber())
                        .ifPresent(room -> orderRoomClasses.put(order.getOrderNumber(), room.getRoomClass()));
            } else {
                orderRoomClasses.put(order.getOrderNumber(), "Только услуга");
            }
            
            // Вычисляем стоимость заказа
            int orderCost = orderService.calculateOrderCost(order);
            orderCosts.put(order.getOrderNumber(), orderCost);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("orderServices", orderServices);
        model.addAttribute("servicePrices", servicePrices);
        model.addAttribute("orderRoomClasses", orderRoomClasses);
        model.addAttribute("orderCosts", orderCosts);
        model.addAttribute("isAdmin", true);
        return "orders";
    }

    @PostMapping("/{orderNumber}/paid")
    @PreAuthorize("hasRole('ADMIN')")
    public String markPaid(@PathVariable Integer orderNumber) {
        orderService.markPaid(orderNumber);
        return "redirect:/orders/admin/all";
    }

    @GetMapping("/{orderNumber}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editOrderForm(@PathVariable Integer orderNumber, Model model) {
        Order order = orderService.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        List<Room> rooms = roomService.findAll();
        model.addAttribute("order", order);
        model.addAttribute("rooms", rooms);
        return "order-edit-form";
    }

    @PostMapping("/{orderNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateOrder(
            @PathVariable Integer orderNumber,
            @RequestParam(required = false) String checkInDateStr,
            @RequestParam(required = false) String checkOutDateStr,
            @ModelAttribute Order order,
            Model model) {
        try {
            if (checkInDateStr != null && !checkInDateStr.isEmpty()) {
                order.setCheckInDate(LocalDate.parse(checkInDateStr));
            }
            if (checkOutDateStr != null && !checkOutDateStr.isEmpty()) {
                order.setCheckOutDate(LocalDate.parse(checkOutDateStr));
            }

            orderService.updateOrder(orderNumber, order);
            return "redirect:/orders/admin/all";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("order", order);
            List<Room> rooms = roomService.findAll();
            model.addAttribute("rooms", rooms);
            return "order-edit-form";
        }
    }

    @GetMapping("/{orderNumber}/services")
    @PreAuthorize("hasRole('USER')")
    public String addServiceToOrderForm(@PathVariable Integer orderNumber, Model model, Authentication authentication) {
        Order order = orderService.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        // Проверяем, что заказ принадлежит текущему пользователю
        String username = authentication.getName();
        String passportNumber = userService.findByUsername(username)
                .map(u -> u.getPassportNumber())
                .orElse(username);
        
        if (!order.getClientPassportNumber().equals(passportNumber)) {
            throw new RuntimeException("У вас нет доступа к этому заказу");
        }
        
        List<org.example.model.AdditionalService> services = additionalServiceRepository.findAll();
        Map<Long, Integer> servicePrices = new HashMap<>();
        
        // Получаем уже добавленные услуги к заказу
        List<Long> addedServiceIds = linkService.findServicesForOrder(orderNumber).stream()
                .map(org.example.model.AdditionalService::getId)
                .collect(java.util.stream.Collectors.toList());
        
        // Получаем цены для всех услуг
        for (org.example.model.AdditionalService service : services) {
            int servicePrice = linkService.getServicePrice(service.getId());
            servicePrices.put(service.getId(), servicePrice);
        }
        
        // Вычисляем текущую стоимость заказа
        int currentOrderCost = orderService.calculateOrderCost(order);
        
        model.addAttribute("order", order);
        model.addAttribute("services", services);
        model.addAttribute("servicePrices", servicePrices);
        model.addAttribute("addedServiceIds", addedServiceIds);
        model.addAttribute("addedServices", linkService.findServicesForOrder(orderNumber));
        model.addAttribute("currentOrderCost", currentOrderCost);
        return "order-services-form";
    }

    @PostMapping("/{orderNumber}/services")
    @PreAuthorize("hasRole('USER')")
    public String addServiceToOrder(@PathVariable Integer orderNumber, @RequestParam Long serviceId, Model model, Authentication authentication) {
        try {
            // Проверяем, что заказ принадлежит текущему пользователю
            Order order = orderService.findById(orderNumber)
                    .orElseThrow(() -> new RuntimeException("Заказ не найден"));
            
            String username = authentication.getName();
            String passportNumber = userService.findByUsername(username)
                    .map(u -> u.getPassportNumber())
                    .orElse(username);
            
            if (!order.getClientPassportNumber().equals(passportNumber)) {
                throw new RuntimeException("У вас нет доступа к этому заказу");
            }
            
            linkService.addServiceToOrder(orderNumber, serviceId);
            return "redirect:/orders";
        } catch (RuntimeException e) {
            Order order = orderService.findById(orderNumber)
                    .orElseThrow(() -> new RuntimeException("Заказ не найден"));
            
            List<org.example.model.AdditionalService> services = additionalServiceRepository.findAll();
            Map<Long, Integer> servicePrices = new HashMap<>();
            
            // Получаем уже добавленные услуги к заказу
            List<Long> addedServiceIds = linkService.findServicesForOrder(orderNumber).stream()
                    .map(org.example.model.AdditionalService::getId)
                    .collect(java.util.stream.Collectors.toList());
            
            // Получаем цены для всех услуг
            for (org.example.model.AdditionalService service : services) {
                int servicePrice = linkService.getServicePrice(service.getId());
                servicePrices.put(service.getId(), servicePrice);
            }
            
            // Вычисляем текущую стоимость заказа
            int currentOrderCost = orderService.calculateOrderCost(order);
            
            model.addAttribute("order", order);
            model.addAttribute("services", services);
            model.addAttribute("servicePrices", servicePrices);
            model.addAttribute("addedServiceIds", addedServiceIds);
            model.addAttribute("addedServices", linkService.findServicesForOrder(orderNumber));
            model.addAttribute("currentOrderCost", currentOrderCost);
            model.addAttribute("error", e.getMessage());
            return "order-services-form";
        }
    }

    @PostMapping("/{orderNumber}/services/{serviceId}/delete")
    @PreAuthorize("hasRole('USER')")
    public String removeServiceFromOrder(@PathVariable Integer orderNumber, @PathVariable Long serviceId, Authentication authentication) {
        // Проверяем, что заказ принадлежит текущему пользователю
        Order order = orderService.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        String username = authentication.getName();
        String passportNumber = userService.findByUsername(username)
                .map(u -> u.getPassportNumber())
                .orElse(username);
        
        if (!order.getClientPassportNumber().equals(passportNumber)) {
            throw new RuntimeException("У вас нет доступа к этому заказу");
        }
        
        linkService.removeServiceFromOrder(orderNumber, serviceId);
        return "redirect:/orders";
    }

    @PostMapping("/{orderNumber}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteOrder(@PathVariable Integer orderNumber) {
        orderService.deleteOrder(orderNumber);
        return "redirect:/orders/admin/all";
    }
}


