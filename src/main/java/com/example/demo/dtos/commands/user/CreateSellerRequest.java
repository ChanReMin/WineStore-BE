package com.example.demo.dtos.commands.user;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSellerRequest {
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}
