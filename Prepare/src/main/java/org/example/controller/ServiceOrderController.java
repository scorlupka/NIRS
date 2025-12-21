package org.example.controller;

import org.example.model.AdditionalService;
import org.example.model.Order;
import org.example.model.OrderServiceLink;
import org.example.repository.AdditionalServiceRepository;
import org.example.repository.OrderServiceLinkRepository;
import org.example.service.OrderService;
import org.example.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/services")
@PreAuthorize("hasRole('USER')")
public class ServiceOrderController {

    private final AdditionalServiceRepository additionalServiceRepository;
    private final OrderServiceLinkRepository linkRepository;
    private final UserService userService;
    private final OrderService orderService;

    public ServiceOrderController(AdditionalServiceRepository additionalServiceRepository,
                                 OrderServiceLinkRepository linkRepository,
                                 UserService userService,
                                 OrderService orderService) {
        this.additionalServiceRepository = additionalServiceRepository;
        this.linkRepository = linkRepository;
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/order")
    public String orderServiceForm(Model model) {
        List<AdditionalService> services = additionalServiceRepository.findAll();
        model.addAttribute("services", services);
        return "service-order-form";
    }

    @PostMapping("/order")
    public String createServiceOrder(@RequestParam Long serviceId, Authentication auth, Model model) {
        try {
            AdditionalService service = additionalServiceRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

            String username = auth.getName();
            String passportNumber = userService.findByUsername(username)
                    .map(u -> u.getPassportNumber())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // Создаем заказ без комнаты (roomNumber = 0 означает заказ только услуги)
            Order order = new Order();
            order.setClientPassportNumber(passportNumber);
            order.setCheckInDate(LocalDate.now());
            order.setCheckOutDate(LocalDate.now());
            order.setGuestsCount(1);
            order.setRoomNumber(0); // 0 означает заказ только услуги
            order.setPaymentStatus("UNPAID");

            // Сохраняем заказ
            Order savedOrder = orderService.createOrder(order);
            
            // Привязываем услугу к заказу
            OrderServiceLink link = new OrderServiceLink(savedOrder.getOrderNumber(), service.getId());
            linkRepository.save(link);
            
            return "redirect:/orders";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            List<AdditionalService> services = additionalServiceRepository.findAll();
            model.addAttribute("services", services);
            return "service-order-form";
        }
    }
}

