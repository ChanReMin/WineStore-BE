package com.example.demo.models.dtos.requests.commands;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandCreateRequest {
    private String name;
    private String country;
    private String description;
}
