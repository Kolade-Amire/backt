package com.kolade.backt.exception;

public class CustomBacktException extends RuntimeException{

    public CustomBacktException(String message) {
        super(message);
    }

    public CustomBacktException(String message, Throwable cause) {
        super(message, cause);
    }
}
