package com.example.demo.dtos.commands.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkActionRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "^(update_status|update_role|delete|send_notification)$",
            message = "Action must be one of: update_status, update_role, delete, send_notification")
    private String action;

    @NotEmpty(message = "User IDs list cannot be empty")
    @Size(max = 100, message = "Maximum 100 users per bulk action")
    private List<String> userIds;

    private Map<String, Object> data;
}