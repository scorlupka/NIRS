package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Добро пожаловать в API");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("login", "POST /api/auth/login");
        endpoints.put("register", "POST /api/auth/register");
        response.put("endpoints", endpoints);
        
        return response;
    }
}

