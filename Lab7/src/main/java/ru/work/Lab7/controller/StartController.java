package ru.work.Lab7.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.work.Lab7.DTO.StudentDTO;
import ru.work.Lab7.ExceptionHandlers.MyAccessDeniedException;
import ru.work.Lab7.model.Student;
import ru.work.Lab7.service.impl.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;

@RestController
@RequestMapping("/api/v2")
public class StartController {

    private final UserService userService;

    @Autowired
    public StartController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Получить список всех студентов", description = "Возвращает список всех студентов. Доступно для ролей TEACHER и STUDENT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получен список студентов"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public List<Student> findAll() {
        return userService.findAll();
    }

    @Operation(summary = "Создать нового студента", description = "Создает нового студента. Требуется роль TEACHER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Студент успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (требуется роль TEACHER)")
    })
    @PostMapping("/")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public void createStudent(@RequestBody StudentDTO studentDTO) {
        userService.createStudent(studentDTO);
    }

    @Operation(summary = "Удалить студента", description = "Удаляет студента по его ID. Требуется роль TEACHER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Студент успешно удален"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (требуется роль TEACHER)"),
            @ApiResponse(responseCode = "404", description = "Студент не найден")
    })
    @DeleteMapping("/students/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public void delete(@Parameter(description = "ID студента") @PathVariable(name = "id") int id) {
        userService.delete(id);
    }

    @Operation(summary = "Изменить баланс любого студента", description = "Изменяет сумму денег студента по его ID. Требуется роль TEACHER")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно изменен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (требуется роль TEACHER)"),
            @ApiResponse(responseCode = "404", description = "Студент не найден")
    })
    @PutMapping("/students/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public void changeMoney(
            @Parameter(description = "ID студента") @PathVariable int id,
            @Parameter(description = "Разница в балансе") @RequestParam int diff) {
        userService.changeMoney(id, diff);
    }

    @Operation(summary = "Изменить свой баланс", description = "Уменьшает вашу сумму денег. Требуется роль STUDENT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно изменен"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен (требуется роль STUDENT)"),
            @ApiResponse(responseCode = "404", description = "Студент не найден")
    })
    @PutMapping("/changeMoney")
    @PreAuthorize("hasRole('STUDENT')")
    public void selfChangeMoney(
            @Parameter(description = "Разница в балансе") @RequestParam int diff,
            Principal principal) {

        int id = Integer.valueOf(principal.getName());
        diff = (-1)*abs(diff);
        userService.changeMoney(id, diff);
    }

    @Operation(summary = "Статус", description = "Получить статус о работе сервера")
    @GetMapping("/getStatus")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Application is running");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}