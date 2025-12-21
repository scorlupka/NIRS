package org.example.controller;

import org.example.model.Room;
import org.example.model.RoomPrice;
import org.example.model.AdditionalService;
import org.example.model.ServicePrice;
import org.example.repository.RoomPriceRepository;
import org.example.repository.ServicePriceRepository;
import org.example.repository.RoomRepository;
import org.example.repository.AdditionalServiceRepository;
import org.example.service.RoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/prices")
@PreAuthorize("hasRole('ADMIN')")
public class PriceController {

    private final RoomPriceRepository roomPriceRepository;
    private final ServicePriceRepository servicePriceRepository;
    private final RoomRepository roomRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final RoomService roomService;

    public PriceController(RoomPriceRepository roomPriceRepository,
                          ServicePriceRepository servicePriceRepository,
                          RoomRepository roomRepository,
                          AdditionalServiceRepository additionalServiceRepository,
                          RoomService roomService) {
        this.roomPriceRepository = roomPriceRepository;
        this.servicePriceRepository = servicePriceRepository;
        this.roomRepository = roomRepository;
        this.additionalServiceRepository = additionalServiceRepository;
        this.roomService = roomService;
    }

    @GetMapping
    public String listPrices(Model model) {
        // Получаем все цены номеров
        List<RoomPrice> roomPrices = roomPriceRepository.findAll();
        Map<Integer, Integer> roomPriceMap = new HashMap<>();
        for (RoomPrice price : roomPrices) {
            roomPriceMap.put(price.getRoomNumber(), price.getBasePrice());
        }
        
        // Получаем все номера для отображения
        List<Room> rooms = roomRepository.findAll();
        
        // Получаем все цены услуг
        List<ServicePrice> servicePrices = servicePriceRepository.findAll();
        Map<Long, Integer> servicePriceMap = new HashMap<>();
        for (ServicePrice price : servicePrices) {
            servicePriceMap.put(price.getServiceId(), price.getBasePrice());
        }
        
        // Получаем все услуги для отображения
        List<AdditionalService> services = additionalServiceRepository.findAll();
        
        model.addAttribute("roomPrices", roomPrices);
        model.addAttribute("roomPriceMap", roomPriceMap);
        model.addAttribute("rooms", rooms);
        model.addAttribute("servicePrices", servicePrices);
        model.addAttribute("servicePriceMap", servicePriceMap);
        model.addAttribute("services", services);
        return "prices";
    }
}

