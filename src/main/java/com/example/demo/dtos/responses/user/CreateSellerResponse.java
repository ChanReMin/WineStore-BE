package com.example.demo.dtos.responses.user;

import com.example.demo.commons.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSellerResponse {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private Gender gender;

    private String role;

    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createdAt;
}
