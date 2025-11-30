package com.example.demo.dtos.commands.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeUserStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(active|inactive|banned)$",
            message = "Status must be one of: active, inactive, banned")
    private String status;

    private String reason;
}
