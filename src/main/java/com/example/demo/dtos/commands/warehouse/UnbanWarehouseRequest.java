package com.example.demo.dtos.commands.warehouse;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnbanWarehouseRequest {

    @Size(max = 1000, message = "Note not exceed 1000 characters")
    private String note;
}