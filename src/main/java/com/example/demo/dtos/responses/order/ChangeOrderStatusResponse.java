package com.example.demo.dtos.responses.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude()
public class ChangeOrderStatusResponse {
    private Long id;
    private Integer status;
    private String statusText;
}
