package org.example.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, Model model) {
        String errorMessage = e.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "Произошла ошибка: " + e.getClass().getSimpleName();
        }
        model.addAttribute("error", errorMessage);
        // Логируем полный стек для отладки
        e.printStackTrace();
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        String errorMessage = "Произошла непредвиденная ошибка: " + e.getMessage();
        if (e.getCause() != null) {
            errorMessage += " (Причина: " + e.getCause().getMessage() + ")";
        }
        model.addAttribute("error", errorMessage);
        // Логируем полный стек для отладки
        e.printStackTrace();
        return "error";
    }
}





