package com.example.demo.dtos.commands.profile;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    @Min(value = 0, message = "Gender must be 0 (Male), 1 (Female), or 2 (Other)")
    @Max(value = 2, message = "Gender must be 0 (Male), 1 (Female), or 2 (Other)")
    private Integer gender;
}