package com.example.demo.dtos.commands.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeUserRoleRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(customer|seller)$",
            message = "Status must be one of: customer, seller")
    private String role;

    private String reason;
}
