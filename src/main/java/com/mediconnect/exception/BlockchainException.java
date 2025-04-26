package com.mediconnect.exception;

public class BlockchainException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public BlockchainException(String message) {
        super(message);
    }
    
    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}