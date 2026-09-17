package com.temi.banking_backend.repository;

import com.temi.banking_backend.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String > {
    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findAllByOwnerId(String ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") String id);
}
