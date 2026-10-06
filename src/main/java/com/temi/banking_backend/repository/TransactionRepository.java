package com.temi.banking_backend.repository;

import com.temi.banking_backend.entity.Transaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    boolean existsByPerformedById(String performedById);

    @Query(value = """
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.fromAccount
        LEFT JOIN FETCH t.toAccount
        JOIN FETCH t.performedBy
        WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId)
        AND t.initiatedAt BETWEEN :from AND :to
        ORDER BY t.initiatedAt DESC
        """,
            countQuery = """
        SELECT COUNT(t) FROM Transaction t
        WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId)
        AND t.initiatedAt BETWEEN :from AND :to
        """)
    Page<Transaction> findStatementForAccount(
            @Param("accountId") String accountId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.id = :id")
    Optional<Transaction> findByIdForUpdate(@Param("id") String id);
}
