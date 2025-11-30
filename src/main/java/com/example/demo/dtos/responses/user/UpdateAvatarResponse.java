package com.example.demo.dtos.responses.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAvatarResponse {
    private String avatarUrl;
    private String message;
}
