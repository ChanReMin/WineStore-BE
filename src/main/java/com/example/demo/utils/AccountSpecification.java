package com.example.demo.utils;

import com.example.demo.commons.enums.AccountRole;
import com.example.demo.commons.enums.AccountStatus;
import com.example.demo.entities.Account;
import com.example.demo.entities.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecification {

    public static Specification<Account> withFilters(String search, AccountRole role, AccountStatus status) {
        return (root, query, cb) -> {
            // JOIN FETCH để tránh N+1
            if (query.getResultType() == Account.class) {
                root.fetch("user", JoinType.LEFT);
            }

            Join<Account, User> userJoin = root.join("user", JoinType.LEFT);

            Predicate predicate = cb.conjunction();

            // Search condition
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";

                Predicate firstNamePredicate = cb.like(cb.lower(userJoin.get("firstName")), searchPattern);
                Predicate lastNamePredicate = cb.like(cb.lower(userJoin.get("lastName")), searchPattern);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchPattern);
                Predicate phonePredicate = cb.like(userJoin.get("phoneNumber"), "%" + search + "%");

                predicate = cb.and(predicate, cb.or(firstNamePredicate, lastNamePredicate, emailPredicate, phonePredicate));
            }

            // Role filter
            if (role != null) {
                predicate = cb.and(predicate, cb.equal(root.get("role"), role));
            }

            // Status filter
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            // Exclude ADMIN
            predicate = cb.and(predicate, cb.notEqual(root.get("role"), AccountRole.ADMIN));

            return predicate;
        };
    }
}