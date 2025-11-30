package com.example.demo.dtos.responses.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeUserStatusResponse {

    private String id;

    private String status;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
