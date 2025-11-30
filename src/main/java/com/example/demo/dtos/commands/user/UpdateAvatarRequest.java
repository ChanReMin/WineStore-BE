package com.example.demo.dtos.commands.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAvatarRequest {
    @NotNull(message = "Avatar file is required")
    private MultipartFile avatar;
}
