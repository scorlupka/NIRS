package org.example.service;

import org.example.model.AdditionalService;
import org.example.model.Order;
import org.example.model.OrderServiceLink;
import org.example.repository.AdditionalServiceRepository;
import org.example.repository.OrderRepository;
import org.example.repository.OrderServiceLinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdditionalServiceLinkService {

    private final OrderService orderService;
    private final OrderServiceLinkRepository linkRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final OrderRepository orderRepository;

    public AdditionalServiceLinkService(OrderService orderService,
                                        OrderServiceLinkRepository linkRepository,
                                        AdditionalServiceRepository additionalServiceRepository,
                                        OrderRepository orderRepository) {
        this.orderService = orderService;
        this.linkRepository = linkRepository;
        this.additionalServiceRepository = additionalServiceRepository;
        this.orderRepository = orderRepository;
    }

    public void addServiceToOrder(Integer orderNumber, Long serviceId) {
        Order order = orderService.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        AdditionalService service = additionalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        OrderServiceLink link = new OrderServiceLink(order.getOrderNumber(), service.getId());
        linkRepository.save(link);
    }

    public List<AdditionalService> findServicesForOrder(Integer orderNumber) {
        List<Long> ids = linkRepository.findServiceIdsByOrderNumber(orderNumber);
        return additionalServiceRepository.findAllById(ids);
    }

    public void removeServiceFromOrder(Integer orderNumber, Long serviceId) {
        linkRepository.deleteByOrderNumberAndServiceId(orderNumber, serviceId);
    }
}


