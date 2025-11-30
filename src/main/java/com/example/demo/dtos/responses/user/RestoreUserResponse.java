package com.example.demo.dtos.responses.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestoreUserResponse {

    private String id;

    private String status;

    @JsonProperty("restoredAt")
    private LocalDateTime restoredAt;
}
