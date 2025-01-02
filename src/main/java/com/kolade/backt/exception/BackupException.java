package com.kolade.backt.exception;

public class BackupException extends RuntimeException {
    public BackupException(String message){
        super(message);
    }
    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }
}