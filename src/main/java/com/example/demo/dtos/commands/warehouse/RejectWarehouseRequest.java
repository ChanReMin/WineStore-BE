package com.example.demo.dtos.commands.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectWarehouseRequest {

    @NotBlank(message = "Lý do từ chối là bắt buộc")
    @Size(max = 1000, message = "Lý do không quá 1000 ký tự")
    private String reason;
}
