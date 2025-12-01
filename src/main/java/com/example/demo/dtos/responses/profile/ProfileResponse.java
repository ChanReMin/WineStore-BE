package com.example.demo.dtos.responses.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String avatar;
    private String phoneNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Integer gender;
    private String role;
    private Integer loyaltyPoints;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}