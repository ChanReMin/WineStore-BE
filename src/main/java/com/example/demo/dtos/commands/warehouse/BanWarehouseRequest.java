package com.example.demo.dtos.commands.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanWarehouseRequest {

    @NotBlank(message = "Reason is required")
    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason;
}
