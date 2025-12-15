package org.example.controller;

import org.example.model.Price;
import org.example.model.Room;
import org.example.service.RoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/rooms")
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String listRooms(Model model) {
        List<Room> rooms = roomService.findAll();
        model.addAttribute("rooms", rooms);
        return "rooms";
    }

    @GetMapping("/new")
    public String newRoomForm(Model model) {
        model.addAttribute("room", new Room());
        return "room-form";
    }

    @PostMapping
    public String createRoom(@ModelAttribute Room room,
                             @RequestParam("basePrice") Integer basePrice) {
        roomService.save(room);
        if (basePrice != null) {
            roomService.saveRoomPrice(room.getRoomNumber(), basePrice);
        }
        return "redirect:/admin/rooms";
    }

    @GetMapping("/{roomNumber}/edit")
    public String editRoom(@PathVariable Integer roomNumber, Model model) {
        Room room = roomService.findById(roomNumber)
                .orElseThrow(() -> new RuntimeException("Номер не найден"));
        Price price = roomService.findPriceForRoom(roomNumber).orElse(null);
        model.addAttribute("room", room);
        model.addAttribute("price", price);
        return "room-form";
    }

    @PostMapping("/{roomNumber}")
    public String updateRoom(@PathVariable Integer roomNumber,
                             @ModelAttribute Room room,
                             @RequestParam("basePrice") Integer basePrice) {
        room.setRoomNumber(roomNumber);
        roomService.save(room);
        if (basePrice != null) {
            roomService.saveRoomPrice(roomNumber, basePrice);
        }
        return "redirect:/admin/rooms";
    }

    @PostMapping("/{roomNumber}/delete")
    public String deleteRoom(@PathVariable Integer roomNumber) {
        roomService.delete(roomNumber);
        return "redirect:/admin/rooms";
    }
}


