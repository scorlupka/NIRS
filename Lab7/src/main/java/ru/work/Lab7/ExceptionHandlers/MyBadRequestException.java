package ru.work.Lab7.ExceptionHandlers;

public class MyBadRequestException extends RuntimeException{

    String message;

    public MyBadRequestException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
