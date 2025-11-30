package com.example.demo.dtos.commands.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendNotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @Pattern(regexp = "^(info|success|warning|error)$",
            message = "Type must be one of: info, success, warning, error")
    private String type = "info";

    private Boolean sendEmail = false;
}
