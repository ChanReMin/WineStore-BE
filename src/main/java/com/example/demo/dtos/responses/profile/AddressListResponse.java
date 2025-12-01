package com.example.demo.dtos.responses.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressListResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<AddressDetail> addresses;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String fullName;
        private String phoneNumber;
        private String addressLine;
        private String ward;
        private String district;
        private String city;
        private String country;
        private Boolean isDefault;
        private String addressType;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime createdAt;
    }
}

