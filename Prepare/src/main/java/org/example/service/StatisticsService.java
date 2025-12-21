package org.example.service;

import org.example.model.AdditionalService;
import org.example.model.Order;
import org.example.repository.AdditionalServiceRepository;
import org.example.repository.OrderRepository;
import org.example.repository.OrderServiceLinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final OrderRepository orderRepository;
    private final OrderServiceLinkRepository orderServiceLinkRepository;
    private final AdditionalServiceRepository additionalServiceRepository;

    public StatisticsService(OrderRepository orderRepository,
                            OrderServiceLinkRepository orderServiceLinkRepository,
                            AdditionalServiceRepository additionalServiceRepository) {
        this.orderRepository = orderRepository;
        this.orderServiceLinkRepository = orderServiceLinkRepository;
        this.additionalServiceRepository = additionalServiceRepository;
    }

    public int getTotalOrdersCost() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .mapToInt(Order::getTotalCost)
                .sum();
    }

    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    public long getPaidOrdersCount() {
        return orderRepository.findAll().stream()
                .filter(order -> "PAID".equals(order.getPaymentStatus()))
                .count();
    }

    public long getUnpaidOrdersCount() {
        return orderRepository.findAll().stream()
                .filter(order -> "UNPAID".equals(order.getPaymentStatus()))
                .count();
    }

    public int getPaidOrdersCost() {
        return orderRepository.findAll().stream()
                .filter(order -> "PAID".equals(order.getPaymentStatus()))
                .mapToInt(Order::getTotalCost)
                .sum();
    }

    public AdditionalService getMostPopularService() {
        List<org.example.model.OrderServiceLink> allLinks = orderServiceLinkRepository.findAll();
        
        // Подсчитываем количество заказов для каждой услуги
        Map<Long, Long> serviceCounts = allLinks.stream()
                .collect(Collectors.groupingBy(
                        org.example.model.OrderServiceLink::getServiceId,
                        Collectors.counting()
                ));
        
        if (serviceCounts.isEmpty()) {
            return null;
        }
        
        // Находим услугу с максимальным количеством заказов
        Long mostPopularServiceId = serviceCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        if (mostPopularServiceId == null) {
            return null;
        }
        
        return additionalServiceRepository.findById(mostPopularServiceId).orElse(null);
    }

    public long getMostPopularServiceCount() {
        List<org.example.model.OrderServiceLink> allLinks = orderServiceLinkRepository.findAll();
        
        if (allLinks.isEmpty()) {
            return 0;
        }
        
        Map<Long, Long> serviceCounts = allLinks.stream()
                .collect(Collectors.groupingBy(
                        org.example.model.OrderServiceLink::getServiceId,
                        Collectors.counting()
                ));
        
        return serviceCounts.values().stream()
                .max(Long::compareTo)
                .orElse(0L);
    }
}


