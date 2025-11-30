package com.example.demo.dtos.commands.user;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportUsersFilters {

    private String search;

    private String role; // "buyer", "seller", "customer"

    private String status; // "active", "inactive", "locked"

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}