package com.example.demo.dtos.commands.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportUsersRequest {

    @NotBlank(message = "Format is required")
    @Pattern(regexp = "^(csv)$",
            message = "Format must be: csv")
    private String format;

    private ExportUsersFilters filters;

    private List<String> fields;
}