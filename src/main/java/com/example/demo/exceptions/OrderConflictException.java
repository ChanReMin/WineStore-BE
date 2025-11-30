package com.example.demo.exceptions;

import com.example.demo.dtos.responses.order.UnavailableProductResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class OrderConflictException extends RuntimeException {
    private final List<UnavailableProductResponse> unavailableItems;

    public OrderConflictException(String message, List<UnavailableProductResponse> unavailableItems) {
        super(message);
        this.unavailableItems = unavailableItems;
    }
}
