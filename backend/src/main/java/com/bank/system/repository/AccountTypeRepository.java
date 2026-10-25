package com.bank.system.repository;

import com.bank.system.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountTypeRepository extends JpaRepository<AccountType, Long> {

    Optional<AccountType> findByTypeCode(String typeCode);

    Optional<AccountType> findByTypeCodeAndIsActiveTrue(String typeCode);

    List<AccountType> findByIsActiveTrue();

    @Query("SELECT at FROM AccountType at WHERE at.isActive = true ORDER BY at.typeName")
    List<AccountType> findAllActiveOrdered();

    @Query("SELECT at FROM AccountType at WHERE at.overdraftAllowed = true AND at.isActive = true")
    List<AccountType> findOverdraftAllowedTypes();

    @Query("SELECT at FROM AccountType at WHERE at.interestRate > 0 AND at.isActive = true")
    List<AccountType> findInterestBearingTypes();

    @Query("SELECT at FROM AccountType at WHERE at.minimumBalance > 0 AND at.isActive = true")
    List<AccountType> findTypesWithMinimumBalance();

    @Query("SELECT COUNT(at) FROM AccountType at WHERE at.isActive = true")
    long countActiveTypes();

    boolean existsByTypeCode(String typeCode);

    boolean existsByTypeCodeAndIsActiveTrue(String typeCode);

    @Query("SELECT at.typeCode FROM AccountType at WHERE at.isActive = true")
    List<String> findAllActiveTypeCodes();

    @Query("SELECT at FROM AccountType at WHERE at.typeName LIKE %:name% AND at.isActive = true")
    List<AccountType> searchByTypeName(@Param("name") String name);

    @Query("SELECT at FROM AccountType at WHERE at.dailyWithdrawalLimit IS NOT NULL AND at.isActive = true")
    List<AccountType> findTypesWithWithdrawalLimits();
}