package com.example.demo.dtos.commands.product;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WriteProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 300, message = "Name must not exceed 300 characters")
    private String name;

    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost price must be positive")
    private BigDecimal costPrice;

    @NotNull(message = "Category ID is required")
    @Min(value = 1, message = "Category ID must be positive")
    private Long categoryId;

    @NotNull(message = "Brand ID is required")
    @Min(value = 1, message = "Brand ID must be positive")
    private Long brandId;

    @DecimalMin(value = "0.0", message = "Concentration must be positive")
    @DecimalMax(value = "100.0", message = "Concentration cannot exceed 100%")
    private BigDecimal concentration;

    @Min(value = 0, message = "Volume must be positive")
    private Integer volume;

    @Size(max = 1000, message = "Images must not exceed 1000 characters")
    private String images;
}