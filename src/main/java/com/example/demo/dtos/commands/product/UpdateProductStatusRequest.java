package com.example.demo.dtos.commands.product;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductStatusRequest {

    @NotNull(message = "Status is required")
    @Min(value = 0, message = "Status must be 0 (Pending), 1 (Active), or 2 (Inactive)")
    @Max(value = 2, message = "Status must be 0 (Pending), 1 (Active), or 2 (Inactive)")
    private Integer status;
}
