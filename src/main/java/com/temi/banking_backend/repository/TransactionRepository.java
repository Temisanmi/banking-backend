package com.temi.banking_backend.repository;

import com.temi.banking_backend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.initiatedAt DESC")
    Page<Transaction> findAllByAccountId(@Param("accountId") String accountId, Pageable pageable);

    boolean existsByReference(String reference);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
            "AND t.initiatedAt BETWEEN :from AND :to ORDER BY t.initiatedAt DESC")
    Page<Transaction> findStatementForAccount(
            @Param("accountId") String accountId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}
