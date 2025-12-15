package ru.work.Lab7.ExceptionHandlers;

public class MyNotFoundException extends RuntimeException{

    String message;

    public MyNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
