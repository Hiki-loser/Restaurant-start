package com.sky.exception;

public class CrudEmployeeException extends RuntimeException {
    public CrudEmployeeException() {
    }
    public CrudEmployeeException(String message) {
        super(message);
    }
}
