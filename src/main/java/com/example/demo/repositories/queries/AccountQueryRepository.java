package com.example.demo.repositories.queries;

import com.example.demo.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AccountQueryRepository extends JpaRepository<Account, Long> {
    boolean existsByEmail(String email);
    Optional<Account> findByEmail(String email);

    @Transactional(transactionManager = "writeTransactionManager")
    Account save(Account account);
}
