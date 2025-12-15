package ru.work.Lab7.ExceptionHandlers;

import org.springframework.security.access.AccessDeniedException;

public class MyAccessDeniedException extends AccessDeniedException {

    String message;

    public MyAccessDeniedException(String message) {
        super(message);
    }

    public String getMessage() {
        return message;
    }
}
