package com.temi.banking_backend.repository;

import com.temi.banking_backend.entity.Account;
import com.temi.banking_backend.entity.enums.AccountStatus;
import com.temi.banking_backend.entity.enums.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findAllByOwnerId(String ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.id = :accountId
            AND a.owner.id = :ownerId
            """)
    Optional<Account> findByIdForUpdateAndOwnerId(
            @Param("accountId") String accountId,
            @Param("ownerId") String ownerId
    );

    @Query("SELECT a FROM Account a WHERE a.owner.phoneNumber = :phoneNumber AND a.type = 'CHECKING'")
    Optional<Account> findCheckingAccountByOwnerPhoneNumber(@Param("phoneNumber") String phoneNumber);

    List<Account> findAllByTypeAndStatus(
            AccountType type,
            AccountStatus status
    );

    boolean existsByOwnerId(String ownerId);
}