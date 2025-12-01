package com.example.demo.dtos.commands.profile;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAddressRequest {

    @Size(max = 200, message = "Full name must not exceed 200 characters")
    private String fullName;

    @Pattern(regexp = "^[0-9]{10,20}$", message = "Phone number must be 10-20 digits")
    private String phoneNumber;

    @Size(max = 500, message = "Address line must not exceed 500 characters")
    private String addressLine;

    @Size(max = 100, message = "Ward must not exceed 100 characters")
    private String ward;

    @Size(max = 100, message = "District must not exceed 100 characters")
    private String district;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    private Boolean isDefault;

    @Size(max = 50, message = "Address type must not exceed 50 characters")
    private String addressType;
}