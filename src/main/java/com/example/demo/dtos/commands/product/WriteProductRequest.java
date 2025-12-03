package com.example.demo.dtos.commands.product;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class WriteProductRequest {


    @NotNull(message = "Category ID is required")
    @Min(value = 1, message = "Category ID must be positive")
    private Long categoryId;

    @NotNull(message = "Brand ID is required")
    @Min(value = 1, message = "Brand ID must be positive")
    private Long brandId;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotBlank(message = "Wine type is required")
    private String winetype;

    @NotBlank(message = "Country of production is required")
    private String countryOfProduction;

    private String grapeVariety;

    @DecimalMin(value = "0.0", message = "Concentration must be positive")
    @DecimalMax(value = "100.0", message = "Concentration cannot exceed 100%")
    private BigDecimal concentration;

    private String productionArea;

    @Min(value = 0, message = "Capacity must be positive")
    private Integer capacity;

    private String idealtemperature;

    private String humidity;

    private String avoidLight;

    private String placeTheBottleHorizontally;

    private String avoidVibration;

    private String openedWine;

    private String useWineCabinet;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
}