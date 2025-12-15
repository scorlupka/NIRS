package ru.work.Lab7.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.work.Lab7.DTO.StudentDTO;
import ru.work.Lab7.DTO.UserDTO;
import ru.work.Lab7.ExceptionHandlers.MyNotFoundException;
import ru.work.Lab7.model.Student;
import ru.work.Lab7.security.JwtUtil;
import ru.work.Lab7.service.impl.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/v2/login")
public class AuthentificationController {

    private final UserService userService;

    @Autowired
    public AuthentificationController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "Авторизоваться", description = "Авторизует пользователя с ролью")
    @ApiResponse(responseCode = "200", description = "Сервис доступен")
    @PostMapping("/")
    public String login (@RequestBody UserDTO userDTO){
        List<Student> students = userService.findAll();

        int id = userDTO.getId();
        String role = userDTO.getRole();
        if(!userDTO.getRole().equals("STUDENT") && !userDTO.getRole().equals("TEACHER")){
            role = "STUDENT";
        }

        if(role.equals("STUDENT") && students.stream().noneMatch(s -> s.getId() == userDTO.getId() && s.getName().equals(userDTO.getName()) && s.getLastname().equals(userDTO.getLastname()))){
            throw new MyNotFoundException("No such Student");
        }

        return jwtUtil.generateToken(id,role);
    }

}
