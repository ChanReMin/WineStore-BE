package com.example.demo.dtos.commands.warehouse;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWarehouseRequest {

    @NotBlank(message = "Warehouse name cannot be blank")
    @Size(max = 255, message = "Warehouse name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Warehouse location cannot be blank")
    @Size(max = 512, message = "Warehouse location cannot exceed 512 characters")
    private String location;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}