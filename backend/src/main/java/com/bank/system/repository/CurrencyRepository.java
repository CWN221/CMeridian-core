package com.bank.system.repository;

import com.bank.system.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    Optional<Currency> findByCurrencyCode(String currencyCode);

    Optional<Currency> findByCurrencyCodeAndIsActiveTrue(String currencyCode);

    List<Currency> findByIsActiveTrue();

    List<Currency> findByIsBaseCurrencyTrue();

    @Query("SELECT c FROM Currency c WHERE c.isActive = true ORDER BY c.currencyCode")
    List<Currency> findAllActiveCurrencies();

    @Query("SELECT c FROM Currency c WHERE c.isBaseCurrency = true AND c.isActive = true")
    Optional<Currency> findBaseCurrency();

    @Query("SELECT COUNT(c) FROM Currency c WHERE c.isActive = true")
    long countActiveCurrencies();

    boolean existsByCurrencyCode(String currencyCode);

    boolean existsByCurrencyCodeAndIsActiveTrue(String currencyCode);

    @Query("SELECT c.currencyCode FROM Currency c WHERE c.isActive = true")
    List<String> findAllActiveCurrencyCodes();

    @Query("SELECT c FROM Currency c WHERE c.currencyName LIKE %:name% AND c.isActive = true")
    List<Currency> searchByCurrencyName(@Param("name") String name);
}