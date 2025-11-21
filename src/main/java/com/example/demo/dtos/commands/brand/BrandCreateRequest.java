package com.example.demo.dtos.commands.brand;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandCreateRequest {
    private String name;
    private String country;
    private String description;
}
