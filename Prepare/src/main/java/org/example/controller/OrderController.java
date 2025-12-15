package org.example.controller;

import org.example.model.Order;
import org.example.model.Room;
import org.example.service.OrderService;
import org.example.service.RoomService;
import org.example.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final RoomService roomService;
    private final UserService userService;

    public OrderController(OrderService orderService, RoomService roomService, UserService userService) {
        this.orderService = orderService;
        this.roomService = roomService;
        this.userService = userService;
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
        model.addAttribute("orders", orders);
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
    public String createOrder(@ModelAttribute Order order, Authentication auth) {
        if (order.getClientPassportNumber() == null || order.getClientPassportNumber().isEmpty()) {
            String username = auth.getName();
            String passportNumber = userService.findByUsername(username)
                    .map(u -> u.getPassportNumber())
                    .orElse(username);
            order.setClientPassportNumber(passportNumber);
        }
        orderService.createOrder(order);
        return "redirect:/orders";
    }

    // Список всех заказов для администратора
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public String listAllOrders(Model model) {
        List<Order> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        model.addAttribute("isAdmin", true);
        return "orders";
    }

    @PostMapping("/{orderNumber}/paid")
    @PreAuthorize("hasRole('ADMIN')")
    public String markPaid(@PathVariable Integer orderNumber) {
        orderService.markPaid(orderNumber);
        return "redirect:/orders/admin/all";
    }
}


