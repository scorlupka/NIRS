package org.example.service;

import org.example.dto.LoginDTO;
import org.example.dto.RegisterDTO;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(LoginDTO loginDTO) {
        Optional<User> userOpt = userRepository.findByUsername(loginDTO.getUsername());
        
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Неверные учетные данные пользователя");
        }

        User user = userOpt.get();
        
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Неверные учетные данные пользователя");
        }

        return user;
    }

    public User register(RegisterDTO registerDTO) {
        // Логином используем ФИО
        String username = registerDTO.getNameLastname();

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        // Проверяем уникальность паспорта
        if (userRepository.existsByPassportNumber(registerDTO.getPassportNumber())) {
            throw new RuntimeException("Пользователь с таким номером паспорта уже зарегистрирован");
        }

        validateRequiredFields(registerDTO);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole("USER"); // Всегда USER при регистрации
        user.setPassportNumber(registerDTO.getPassportNumber());
        user.setPassportSeria(registerDTO.getPassportSeria());
        user.setNameLastname(registerDTO.getNameLastname());
        user.setPhone(registerDTO.getPhone());

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    private void validateRequiredFields(RegisterDTO dto) {
        if (isBlank(dto.getUsername()) || isBlank(dto.getPassword()) || isBlank(dto.getPassportNumber())
                || isBlank(dto.getPassportSeria()) || isBlank(dto.getNameLastname()) || isBlank(dto.getPhone())) {
            throw new RuntimeException("Заполните все обязательные поля");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

