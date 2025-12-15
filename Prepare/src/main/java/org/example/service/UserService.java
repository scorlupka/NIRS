package org.example.service;

import org.example.dto.LoginDTO;
import org.example.dto.RegisterDTO;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(LoginDTO loginDTO) {
        Optional<User> userOpt = userRepository.findByUsername(loginDTO.getUsername());
        
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Неверные учетные данные пользователя");
        }

        User user = userOpt.get();
        
        // Сравниваем пароли напрямую (без хеширования)
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("Неверные учетные данные пользователя");
        }

        return user;
    }

    public User register(RegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword()); // Сохраняем пароль в открытом виде
        user.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : "USER");

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}

