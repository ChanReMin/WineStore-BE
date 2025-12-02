package com.example.demo.dtos.commands.order;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class OrderCancelRequest {
    @NotBlank(message = "Cancellation reason cannot be empty")
    @Size(min = 10, message = "Cancellation reason must be at least 10 characters long")
    private String reason;

}
