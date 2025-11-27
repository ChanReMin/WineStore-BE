package com.example.demo.dtos.commands.warehouse;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveWarehouseRequest {

    @Size(max = 1000, message = "Ghi chú không quá 1000 ký tự")
    private String note;
}
