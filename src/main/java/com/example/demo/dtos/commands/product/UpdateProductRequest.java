package com.example.demo.dtos.commands.product;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @NotNull(message = "Category ID is required")
    @Min(value = 0, message = "categoryId must be positive")
    private Long categoryId;

    @NotNull(message = "Brand ID is required")
    @Min(value = 0, message = "brandId must be positive")
    private Long brandId;

    @NotBlank(message = "Product name is required")
    @Size(max = 300, message = "Name must not exceed 300 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Size(max = 100, message = "Wine type must not exceed 100 characters")
    private String wineType;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String countryOfProduction;

    @Size(max = 200, message = "Grape variety must not exceed 200 characters")
    private String grapeVariety;

    @DecimalMin(value = "0.0", message = "Concentration must be positive")
    @DecimalMax(value = "100.0", message = "Concentration cannot exceed 100%")
    private BigDecimal concentration;

    @Size(max = 200, message = "Production area must not exceed 200 characters")
    private String productionArea;

    @Min(value = 0, message = "Capacity must be positive")
    private Integer capacity;

    private String idealTemperature;
    private String humidity;

    @Size(max = 1000, message = "Images must not exceed 1000 characters")
    private String images;

    private String description;
}
