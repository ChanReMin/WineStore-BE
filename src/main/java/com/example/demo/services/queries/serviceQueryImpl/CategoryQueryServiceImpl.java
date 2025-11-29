package com.example.demo.services.queries.serviceQueryImpl;

import com.example.demo.dtos.responses.category.CategoryListItemResponse;
import com.example.demo.repositories.queries.CategoryQueryRepository;
import com.example.demo.services.queries.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryQueryServiceImpl implements CategoryQueryService {

    private final CategoryQueryRepository categoryQueryRepository;

    @Transactional(transactionManager = "readTransactionManager", readOnly = true)
    public List<CategoryListItemResponse> getAllCategories() {
        return categoryQueryRepository.getAllCategoryWithProductCount();
    }
}
