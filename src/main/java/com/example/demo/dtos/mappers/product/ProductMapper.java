package com.example.demo.dtos.mappers.product;

import com.example.demo.dtos.commands.product.CreateProductRequest;
import com.example.demo.dtos.commands.product.UpdateProductRequest;
import com.example.demo.dtos.responses.product.CreateProductResponse;
import com.example.demo.dtos.responses.product.ProductListResponse;
import com.example.demo.entities.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductListResponse toListResponse(Product product) {
        if (product == null) {
            return null;
        }

        // Calculate total inventory from inventories list
        Integer totalInventory = product.getInventories() != null
                ? product.getInventories().stream()
                .mapToInt(inv -> inv.getQuantityOnHand() != null ? inv.getQuantityOnHand() : 0)
                .sum()
                : 0;

        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .category(product.getCategory() != null ? product.getCategory().getName() : null)
                .brand(product.getBrand() != null ? product.getBrand().getName() : null)
                .status(product.getStatus() != null ? product.getStatus().getCode() : null)
                .statusText(product.getStatus() != null ? product.getStatus().getDescription() : null)
                .totalInventory(totalInventory)
                .createdAt(product.getCreatedAt())
                .approvedAt(product.getApprovedAt())
                .approvedBy(product.getApprovedBy() != null ? product.getApprovedBy().getEmail() : null)
                .build();
    }

    public CreateProductResponse toCreateResponse(Product product) {
        if (product == null) {
            return null;
        }

        return CreateProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .status(product.getStatus() != null ? product.getStatus().getCode() : null)
                .statusText(product.getStatus() != null ? product.getStatus().getDescription() : null)
                .build();
    }

    public Product toEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }

        return Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .wineType(request.getWineType())
                .countryOfProduction(request.getCountryOfProduction())
                .grapeVariety(request.getGrapeVariety())
                .concentration(request.getConcentration())
                .productionArea(request.getProductionArea())
                .capacity(request.getCapacity())
                .idealTemperature(request.getIdealTemperature())
                .humidity(request.getHumidity())
                .avoidLight(request.getAvoidLight())
                .placeTheBottleHorizontally(request.getPlaceTheBottleHorizontally())
                .avoidVibration(request.getAvoidVibration())
                .openedWine(request.getOpenedWine())
                .useWineCabinet(request.getUseWineCabinet())
                .images(request.getImages())
                .description(request.getDescription())
                .build();
    }

    public void updateEntity(Product product, UpdateProductRequest request) {
        if (product == null || request == null) {
            return;
        }

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setWineType(request.getWineType());
        product.setCountryOfProduction(request.getCountryOfProduction());
        product.setGrapeVariety(request.getGrapeVariety());
        product.setConcentration(request.getConcentration());
        product.setProductionArea(request.getProductionArea());
        product.setCapacity(request.getCapacity());
        product.setIdealTemperature(request.getIdealTemperature());
        product.setHumidity(request.getHumidity());
        product.setAvoidLight(request.getAvoidLight());
        product.setPlaceTheBottleHorizontally(request.getPlaceTheBottleHorizontally());
        product.setAvoidVibration(request.getAvoidVibration());
        product.setOpenedWine(request.getOpenedWine());
        product.setUseWineCabinet(request.getUseWineCabinet());
        product.setImages(request.getImages());
        product.setDescription(request.getDescription());
    }
}