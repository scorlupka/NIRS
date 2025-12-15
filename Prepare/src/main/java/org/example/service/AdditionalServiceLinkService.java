package org.example.service;

import org.example.model.AdditionalService;
import org.example.model.Order;
import org.example.model.OrderServiceLink;
import org.example.model.Price;
import org.example.repository.AdditionalServiceRepository;
import org.example.repository.OrderServiceLinkRepository;
import org.example.repository.PriceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdditionalServiceLinkService {

    private final OrderService orderService;
    private final OrderServiceLinkRepository linkRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final PriceRepository priceRepository;

    public AdditionalServiceLinkService(OrderService orderService,
                                        OrderServiceLinkRepository linkRepository,
                                        AdditionalServiceRepository additionalServiceRepository,
                                        PriceRepository priceRepository) {
        this.orderService = orderService;
        this.linkRepository = linkRepository;
        this.additionalServiceRepository = additionalServiceRepository;
        this.priceRepository = priceRepository;
    }

    public void addServiceToOrder(Integer orderNumber, Long serviceId) {
        Order order = orderService.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        AdditionalService service = additionalServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

        // Проверяем, не добавлена ли уже услуга
        if (linkRepository.findServiceIdsByOrderNumber(orderNumber).contains(serviceId)) {
            throw new RuntimeException("Услуга уже добавлена к заказу");
        }

        OrderServiceLink link = new OrderServiceLink(order.getOrderNumber(), service.getId());
        linkRepository.save(link);

        // Получаем цену услуги
        Price servicePrice = priceRepository.findFirstByObjectTypeAndObjectNumber("SERVICE", service.getId().intValue())
                .orElse(null);

        // Пересчитываем стоимость заказа
        int serviceCost = servicePrice != null ? servicePrice.getBasePrice() : 0;
        int newTotalCost = order.getTotalCost() + serviceCost;
        order.setTotalCost(newTotalCost);
        
        // Меняем статус оплаты на UNPAID при добавлении услуги
        order.setPaymentStatus("UNPAID");
        
        orderService.saveOrder(order);
    }

    public List<AdditionalService> findServicesForOrder(Integer orderNumber) {
        List<Long> ids = linkRepository.findServiceIdsByOrderNumber(orderNumber);
        return additionalServiceRepository.findAllById(ids);
    }

    public void removeServiceFromOrder(Integer orderNumber, Long serviceId) {
        Order order = orderService.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        // Получаем цену услуги
        Price servicePrice = priceRepository.findFirstByObjectTypeAndObjectNumber("SERVICE", serviceId.intValue())
                .orElse(null);
        
        // Удаляем связь
        linkRepository.deleteByOrderNumberAndServiceId(orderNumber, serviceId);
        
        // Пересчитываем стоимость заказа
        int serviceCost = servicePrice != null ? servicePrice.getBasePrice() : 0;
        int newTotalCost = Math.max(0, order.getTotalCost() - serviceCost);
        order.setTotalCost(newTotalCost);
        
        // Меняем статус оплаты на UNPAID при удалении услуги
        order.setPaymentStatus("UNPAID");
        
        orderService.saveOrder(order);
    }
}



