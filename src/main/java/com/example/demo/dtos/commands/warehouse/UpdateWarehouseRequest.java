package com.example.demo.dtos.commands.warehouse;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWarehouseRequest {

    @Size(max = 255, message = "Tên kho không quá 255 ký tự")
    private String name;

    @Size(max = 512, message = "Địa chỉ không quá 512 ký tự")
    private String location;

    @Size(max = 1000, message = "Mô tả không quá 1000 ký tự")
    private String description;
}