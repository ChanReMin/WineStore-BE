package com.example.demo.dtos.commands.warehouse;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWarehouseRequest {

    @NotBlank(message = "Tên kho không được để trống")
    @Size(max = 255, message = "Tên kho không quá 255 ký tự")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 512, message = "Địa chỉ không quá 512 ký tự")
    private String location;

    @Size(max = 1000, message = "Mô tả không quá 1000 ký tự")
    private String description;
}