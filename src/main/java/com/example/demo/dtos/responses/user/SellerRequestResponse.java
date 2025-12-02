package com.example.demo.dtos.responses.user;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerRequestResponse {
    private Long requestId;
    private String status;
    private LocalDateTime createdAt;
}
