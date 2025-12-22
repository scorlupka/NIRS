package org.example.service;

import org.example.model.AdditionalService;
import org.example.model.Order;
import org.example.model.OrderServiceLink;
import org.example.model.ServicePrice;
import org.example.repository.AdditionalServiceRepository;
import org.example.repository.OrderServiceLinkRepository;
import org.example.repository.ServicePriceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdditionalServiceLinkService {

    private final OrderService orderService;
    private final OrderServiceLinkRepository linkRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final ServicePriceRepository servicePriceRepository;

    public AdditionalServiceLinkService(OrderService orderService,
                                        OrderServiceLinkRepository linkRepository,
                                        AdditionalServiceRepository additionalServiceRepository,
                                        ServicePriceRepository servicePriceRepository) {
        this.orderService = orderService;
        this.linkRepository = linkRepository;
        this.additionalServiceRepository = additionalServiceRepository;
        this.servicePriceRepository = servicePriceRepository;
    }

    @Transactional
    public void addServiceToOrder(Integer orderNumber, Long serviceId) {
        if (orderNumber == null) {
            throw new RuntimeException("Номер заказа не указан");
        }
        if (serviceId == null) {
            throw new RuntimeException("ID услуги не указан");
        }
        
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
        
        // Меняем статус оплаты на UNPAID при добавлении услуги
        order.setPaymentStatus("UNPAID");
        
        try {
            orderService.saveOrder(order);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении заказа: " + e.getMessage(), e);
        }
    }

    public List<AdditionalService> findServicesForOrder(Integer orderNumber) {
        List<Long> ids = linkRepository.findServiceIdsByOrderNumber(orderNumber);
        return additionalServiceRepository.findAllById(ids);
    }

    @Transactional
    public void removeServiceFromOrder(Integer orderNumber, Long serviceId) {
        Order order = orderService.findById(orderNumber)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        // Удаляем связь
        linkRepository.deleteByOrderNumberAndServiceId(orderNumber, serviceId);
        
        // Меняем статус оплаты на UNPAID при удалении услуги
        order.setPaymentStatus("UNPAID");
        
        orderService.saveOrder(order);
    }
    
    public Integer getServicePrice(Long serviceId) {
        return servicePriceRepository.findByServiceId(serviceId)
                .map(ServicePrice::getBasePrice)
                .orElse(0);
    }
    
    @Transactional
    public void deleteAllLinksByServiceId(Long serviceId) {
        linkRepository.deleteByServiceId(serviceId);
    }
}



