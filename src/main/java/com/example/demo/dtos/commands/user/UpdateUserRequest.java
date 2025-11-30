package com.example.demo.dtos.commands.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
    private String phone;

    @Pattern(regexp = "^(seller|admin|customer)$",
            message = "Role must be one of: seller, admin, customer")
    private String role;

    @Pattern(regexp = "^(active|inactive|banned)$",
            message = "Status must be one of: active, inactive, banned")
    private String status;

    private Boolean emailVerified;

    private String notes;
}