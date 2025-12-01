package com.example.demo.dtos.responses.profile;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddAddressResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String fullName;
    private Boolean isDefault;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}
