package com.example.demo.dtos.responses.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportUsersResponse {

    private byte[] fileContent;
    private Long totalRecords;
    private String filename;
}
