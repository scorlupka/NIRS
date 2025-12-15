package org.example.controller;

import org.example.model.AdditionalService;
import org.example.service.AdditionalServiceLinkService;
import org.example.repository.AdditionalServiceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/services")
@PreAuthorize("hasRole('ADMIN')")
public class AdditionalServiceController {

    private final AdditionalServiceRepository additionalServiceRepository;
    private final AdditionalServiceLinkService linkService;

    public AdditionalServiceController(AdditionalServiceRepository additionalServiceRepository,
                                       AdditionalServiceLinkService linkService) {
        this.additionalServiceRepository = additionalServiceRepository;
        this.linkService = linkService;
    }

    @GetMapping
    public String listServices(Model model) {
        List<AdditionalService> services = additionalServiceRepository.findAll();
        model.addAttribute("services", services);
        return "services";
    }

    @GetMapping("/new")
    public String newServiceForm(Model model) {
        model.addAttribute("service", new AdditionalService());
        return "service-form";
    }

    @PostMapping
    public String createService(@ModelAttribute AdditionalService service) {
        if (service.getServiceTime() == null) {
            service.setServiceTime(LocalDateTime.now());
        }
        additionalServiceRepository.save(service);
        return "redirect:/admin/services";
    }

    @GetMapping("/{id}/edit")
    public String editService(@PathVariable Long id, Model model) {
        AdditionalService service = additionalServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Услуга не найдена"));
        model.addAttribute("service", service);
        return "service-form";
    }

    @PostMapping("/{id}")
    public String updateService(@PathVariable Long id, @ModelAttribute AdditionalService service) {
        service.setId(id);
        if (service.getServiceTime() == null) {
            service.setServiceTime(LocalDateTime.now());
        }
        additionalServiceRepository.save(service);
        return "redirect:/admin/services";
    }

    @PostMapping("/{id}/delete")
    public String deleteService(@PathVariable Long id) {
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


