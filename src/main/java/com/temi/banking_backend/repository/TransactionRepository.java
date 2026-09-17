package com.temi.banking_backend.repository;

import com.temi.banking_backend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.initiatedAt DESC")
    Page<Transaction> findAllByAccountId(@Param("accountId") String accountId, Pageable pageable);
}
