package com.example.demo.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
@Getter
@Setter
public class ConflictException extends RuntimeException {

    private Object data;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    // Inner class for structured conflict data, e.g., for stock issues
    @Getter
    @Setter
    public static class ConflictData {
        private Integer availableQuantity;
        private Integer requestedQuantity;

        public ConflictData(Integer availableQuantity, Integer requestedQuantity) {
            this.availableQuantity = availableQuantity;
            this.requestedQuantity = requestedQuantity;
        }
    }
}
