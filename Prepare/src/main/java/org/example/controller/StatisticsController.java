package org.example.controller;

import org.example.model.AdditionalService;
import org.example.service.StatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public String showStatistics(Model model) {
        try {
            // Общая статистика по заказам
            int totalOrdersCost = statisticsService.getTotalOrdersCost();
            long totalOrdersCount = statisticsService.getTotalOrdersCount();
            long paidOrdersCount = statisticsService.getPaidOrdersCount();
            long unpaidOrdersCount = statisticsService.getUnpaidOrdersCount();
            int paidOrdersCost = statisticsService.getPaidOrdersCost();
            
            // Статистика по услугам
            AdditionalService mostPopularService = statisticsService.getMostPopularService();
            long mostPopularServiceCount = statisticsService.getMostPopularServiceCount();
            
            model.addAttribute("totalOrdersCost", totalOrdersCost);
            model.addAttribute("totalOrdersCount", totalOrdersCount);
            model.addAttribute("paidOrdersCount", paidOrdersCount);
            model.addAttribute("unpaidOrdersCount", unpaidOrdersCount);
            model.addAttribute("paidOrdersCost", paidOrdersCost);
            model.addAttribute("mostPopularService", mostPopularService);
            model.addAttribute("mostPopularServiceCount", mostPopularServiceCount);
            
            return "statistics";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при загрузке статистики: " + e.getMessage());
            return "statistics";
        }
    }
}

