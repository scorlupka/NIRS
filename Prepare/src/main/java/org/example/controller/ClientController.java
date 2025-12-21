package org.example.controller;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/clients")
@PreAuthorize("hasRole('ADMIN')")
public class ClientController {

    private final UserRepository userRepository;

    public ClientController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listClients(Model model) {
        List<User> clients = userRepository.findAll();
        model.addAttribute("clients", clients);
        return "clients";
    }

    @GetMapping("/{passportNumber}/edit")
    public String editClientForm(@PathVariable String passportNumber, Model model) {
        User client = userRepository.findById(passportNumber)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));
        model.addAttribute("client", client);
        return "client-form";
    }

    @PostMapping("/{passportNumber}")
    public String updateClient(@PathVariable String passportNumber, @ModelAttribute User client) {
        User existingClient = userRepository.findById(passportNumber)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));
        
        existingClient.setNameLastname(client.getNameLastname());
        existingClient.setPhone(client.getPhone());
        existingClient.setPassportSeria(client.getPassportSeria());
        existingClient.setRole(client.getRole());
        
        userRepository.save(existingClient);
        return "redirect:/admin/clients";
    }

    @GetMapping("/new")
    public String newClientForm(Model model) {
        model.addAttribute("client", new User());
        return "client-form";
    }

    @PostMapping("/new")
    public String createClient(@ModelAttribute User client) {
        if (userRepository.existsById(client.getPassportNumber())) {
            throw new RuntimeException("Клиент с таким паспортом уже существует");
        }
        userRepository.save(client);
        return "redirect:/admin/clients";
    }

    @PostMapping("/{passportNumber}/delete")
    public String deleteClient(@PathVariable String passportNumber) {
        if (!userRepository.existsById(passportNumber)) {
            throw new RuntimeException("Клиент не найден");
        }
        userRepository.deleteById(passportNumber);
        return "redirect:/admin/clients";
    }
}



