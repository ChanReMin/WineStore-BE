package com.example.demo.services.commands.ServiceCommandImpl;

import com.example.demo.dtos.mappers.BrandMapper;
import com.example.demo.dtos.responses.brand.BrandListItemResponse;
import com.example.demo.entities.Brand;
import com.example.demo.repositories.commands.BrandCommandRepository;
import com.example.demo.services.commands.BrandCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class BrandCommandServiceImpl implements BrandCommandService {
    private final BrandCommandRepository brandCommandRepository;

    @Transactional(transactionManager = "writeTransactionManager")
    public BrandListItemResponse createBrand(Brand brand) {
        Brand savedBrand = brandCommandRepository.save(brand);
        return BrandMapper.toResponse(savedBrand);
    }

}
