package com.bank.system.repository;

import com.bank.system.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    Optional<Branch> findByBranchCode(String branchCode);

    Optional<Branch> findByBranchCodeAndIsActiveTrue(String branchCode);

    List<Branch> findByIsActiveTrue();

    List<Branch> findByCity(String city);

    List<Branch> findByCountry(String country);

    @Query("SELECT b FROM Branch b WHERE b.isActive = true ORDER BY b.branchName")
    List<Branch> findAllActiveOrdered();

    @Query("SELECT b FROM Branch b WHERE b.city = :city AND b.isActive = true")
    List<Branch> findActiveBranchesByCity(@Param("city") String city);

    @Query("SELECT b FROM Branch b WHERE b.manager IS NOT NULL AND b.isActive = true")
    List<Branch> findBranchesWithManager();

    @Query("SELECT b FROM Branch b WHERE b.manager IS NULL AND b.isActive = true")
    List<Branch> findBranchesWithoutManager();

    @Query("SELECT COUNT(b) FROM Branch b WHERE b.isActive = true")
    long countActiveBranches();

    boolean existsByBranchCode(String branchCode);

    boolean existsByBranchCodeAndIsActiveTrue(String branchCode);

    @Query("SELECT b.branchCode FROM Branch b WHERE b.isActive = true")
    List<String> findAllActiveBranchCodes();

    @Query("SELECT b FROM Branch b WHERE " +
           "LOWER(b.branchName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.branchCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.country) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Branch> searchBranches(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT b FROM Branch b WHERE b.country = :country AND b.isActive = true ORDER BY b.city")
    List<Branch> findActiveBranchesByCountry(@Param("country") String country);

    @Query("SELECT b.city, COUNT(b) FROM Branch b WHERE b.isActive = true GROUP BY b.city")
    List<Object[]> getBranchCountByCity();

    @Query("SELECT b.country, COUNT(b) FROM Branch b WHERE b.isActive = true GROUP BY b.country")
    List<Object[]> getBranchCountByCountry();

    @Query("SELECT b FROM Branch b WHERE b.isActive = true AND b.id IN :branchIds")
    List<Branch> findActiveBranchesByIds(@Param("branchIds") List<Long> branchIds);
}