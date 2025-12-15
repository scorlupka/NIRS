package org.example.controller;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}


