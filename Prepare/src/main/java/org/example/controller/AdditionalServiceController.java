package org.example.controller;

import org.example.model.AdditionalService;
import org.example.model.ServicePrice;
import org.example.service.AdditionalServiceLinkService;
import org.example.repository.AdditionalServiceRepository;
import org.example.repository.ServicePriceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/services")
@PreAuthorize("hasRole('ADMIN')")
public class AdditionalServiceController {

    private final AdditionalServiceRepository additionalServiceRepository;
    private final AdditionalServiceLinkService linkService;
    private final ServicePriceRepository servicePriceRepository;

    public AdditionalServiceController(AdditionalServiceRepository additionalServiceRepository,
                                       AdditionalServiceLinkService linkService,
                                       ServicePriceRepository servicePriceRepository) {
        this.additionalServiceRepository = additionalServiceRepository;
        this.linkService = linkService;
        this.servicePriceRepository = servicePriceRepository;
    }

    @GetMapping
    public String listServices(Model model) {
        List<AdditionalService> services = additionalServiceRepository.findAll();
        // Получаем цены для каждой услуги
        java.util.Map<Long, ServicePrice> servicePrices = new java.util.HashMap<>();
        for (AdditionalService service : services) {
            servicePriceRepository.findByServiceId(service.getId())
                    .ifPresent(price -> servicePrices.put(service.getId(), price));
        }
        model.addAttribute("services", services);
        model.addAttribute("servicePrices", servicePrices);
        return "services";
    }

    @GetMapping("/new")
    public String newServiceForm(Model model) {
        model.addAttribute("service", new AdditionalService());
        return "service-form";
    }

    @PostMapping
    public String createService(@ModelAttribute AdditionalService service, @RequestParam("basePrice") Integer basePrice) {
        if (service.getServiceTime() == null || service.getServiceTime().isEmpty()) {
            service.setServiceTime("00:00-23:59");
        }
        AdditionalService savedService = additionalServiceRepository.save(service);
        
        // Сохраняем цену услуги
        if (basePrice != null && basePrice > 0) {
            ServicePrice price = new ServicePrice(savedService.getId(), basePrice);
            servicePriceRepository.save(price);
        }
        
        return "redirect:/admin/services";
    }

    @GetMapping("/{id}/edit")
    public String editService(@PathVariable Long id, Model model) {
        AdditionalService service = additionalServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));
        ServicePrice price = servicePriceRepository.findByServiceId(id).orElse(null);
        model.addAttribute("service", service);
        model.addAttribute("price", price);
        return "service-form";
    }

    @PostMapping("/{id}")
    public String updateService(@PathVariable Long id, @ModelAttribute AdditionalService service, @RequestParam("basePrice") Integer basePrice) {
        service.setId(id);
        if (service.getServiceTime() == null || service.getServiceTime().isEmpty()) {
            service.setServiceTime("00:00-23:59");
        }
        additionalServiceRepository.save(service);
        
        // Обновляем или создаем цену услуги
        ServicePrice existingPrice = servicePriceRepository.findByServiceId(id).orElse(null);
        if (existingPrice != null) {
            existingPrice.setBasePrice(basePrice);
            servicePriceRepository.save(existingPrice);
        } else if (basePrice != null && basePrice > 0) {
            ServicePrice price = new ServicePrice(id, basePrice);
            servicePriceRepository.save(price);
        }
        
        return "redirect:/admin/services";
    }

    @PostMapping("/{id}/delete")
    @org.springframework.transaction.annotation.Transactional
    public String deleteService(@PathVariable Long id) {
        // Удаляем все связи услуги с заказами
        linkService.deleteAllLinksByServiceId(id);
        
        // Удаляем цену услуги
        servicePriceRepository.findByServiceId(id)
                .ifPresent(servicePriceRepository::delete);
        
        // Теперь можно безопасно удалить услугу
        additionalServiceRepository.deleteById(id);
        return "redirect:/admin/services";
    }

    // Привязка услуги к заказу
    @PostMapping("/attach")
    public String attachToOrder(@RequestParam Integer orderNumber,
                                @RequestParam Long serviceId) {
        linkService.addServiceToOrder(orderNumber, serviceId);
        return "redirect:/orders/admin/all";
    }
}



