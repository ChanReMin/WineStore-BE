package com.example.demo.dtos.commands.warehouse;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWarehouseRequest {

    @Size(max = 255, message = "Name not exceed 255 characters")
    private String name;

    @Size(max = 512, message = "Location not exceed 512 characters")
    private String location;

    @Size(max = 1000, message = "Description not exceed 1000 characters")
    private String description;
}