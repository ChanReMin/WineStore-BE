package com.example.demo.repositories.commands;

import com.example.demo.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountCommandRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);

    /**
     * Batch update status for multiple accounts
     */
    @Modifying
    @Query("UPDATE Account a SET a.status = :status WHERE a.id IN :ids")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") com.example.demo.commons.enums.AccountStatus status);

    /**
     * Batch update role for multiple accounts
     */
    @Modifying
    @Query("UPDATE Account a SET a.role = :role WHERE a.id IN :ids")
    int batchUpdateRole(@Param("ids") List<Long> ids, @Param("role") com.example.demo.commons.enums.AccountRole role);

    /**
     * Soft delete by setting status to INACTIVE
     */
    @Modifying
    @Query("UPDATE Account a SET a.status = com.example.demo.commons.enums.AccountStatus.INACTIVE WHERE a.id = :id")
    int softDelete(@Param("id") Long id);

    /**
     * Batch soft delete
     */
    @Modifying
    @Query("UPDATE Account a SET a.status = com.example.demo.commons.enums.AccountStatus.INACTIVE WHERE a.id IN :ids")
    int batchSoftDelete(@Param("ids") List<Long> ids);
}
