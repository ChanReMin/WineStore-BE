package com.example.demo.dtos.commands.warehouse;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWarehouseRequest {

    @NotBlank(message = "Warehouse name cannot be blank")
    @Size(max = 255, message = "Warehouse name cannot exceed 255 characters")
    private String name;

    @NotBlank(message = "Warehouse location cannot be blank")
    @Size(max = 512, message = "Địa chỉ không quá 512 ký tự")
    private String location;

    @NotBlank(message = "Thành phố không được để trống")
    @Size(max = 50, message = "Thành phố không quá 50 ký tự")
    private String city;

    @Size(max = 1000, message = "Mô tả không quá 1000 ký tự")
    private String description;
}