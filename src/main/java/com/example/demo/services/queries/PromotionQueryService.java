package com.example.demo.services.queries;

import com.example.demo.entities.Promotion;
import com.example.demo.repositories.queries.PromotionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.example.demo.configs.SecurityUtils; // Import SecurityUtils

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionQueryService {

    private final PromotionQueryRepository promotionQueryRepository;

    public Page<Promotion> getSellerPromotions(
            Optional<Integer> page,
            Optional<Integer> limit,
            Optional<Integer> status,
            Optional<String> search,
            Optional<String> sortBy,
            Optional<String> sortOrder
    ) {
        int pageNo = page.orElse(1) - 1; // Page numbers are 0-indexed in Spring Data JPA
        int pageSize = limit.orElse(20);

        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder.orElse("desc")), sortBy.orElse("createdAt"));

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Promotion> spec = (root, query, cb) -> {
            // Start with a true predicate to allow easy combining
            jakarta.persistence.criteria.Predicate predicate = cb.conjunction();

            // Filter by status
            if (status.isPresent()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status.get()));
            }

            // Search by name or code
            if (search.isPresent() && StringUtils.hasText(search.get())) {
                String searchTerm = "%" + search.get().toLowerCase() + "%";
                jakarta.persistence.criteria.Predicate nameLike = cb.like(cb.lower(root.get("name")), searchTerm);
                jakarta.persistence.criteria.Predicate codeLike = cb.like(cb.lower(root.get("code")), searchTerm);
                predicate = cb.and(predicate, cb.or(nameLike, codeLike));
            }

            // Ensure promotions belong to the current seller (assuming createdBy is the seller)
            Long currentUserId = SecurityUtils.getCurrentUserUuid();
            if (currentUserId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("createdBy").get("id"), currentUserId));
            }


            return predicate;
        };

        return promotionQueryRepository.findAll(spec, pageable);
    }
}
