package com.example.demo.services.queries;

import com.example.demo.dtos.responses.brand.BrandListItemResponse;
import com.example.demo.repositories.queries.BrandQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandQueryService {
    private final BrandQueryRepository brandQueryRepository;

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public List<BrandListItemResponse> getAllBrands() {
        return brandQueryRepository.getAllBrandWithProductCount();
    }
}
