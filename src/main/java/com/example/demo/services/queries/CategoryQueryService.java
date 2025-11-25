package com.example.demo.services.queries;

import com.example.demo.dtos.responses.SuccessResponse;
import com.example.demo.dtos.responses.category.CategoryListItemResponse;
import com.example.demo.dtos.responses.category.CategoryListResponse;
import com.example.demo.repositories.queries.CategoryQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryQueryRepository categoryQueryRepository;

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public List<CategoryListItemResponse> getAllCategories() {
        return categoryQueryRepository.getAllCategoryWithProductCount();
    }
}
