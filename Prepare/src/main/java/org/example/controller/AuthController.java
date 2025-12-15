package org.example.controller;

import org.example.dto.LoginDTO;
import org.example.dto.RegisterDTO;
import org.example.model.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            User user = userService.authenticate(loginDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Успешный вход");
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("id", user.getId());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO registerDTO) {
        try {
            User user = userService.register(registerDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Пользователь успешно зарегистрирован");
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("id", user.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}

