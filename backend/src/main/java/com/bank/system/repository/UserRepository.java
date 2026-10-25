package com.bank.system.repository;

import com.bank.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmployeeId(Long employeeId);

    boolean existsByUsername(String username);

    boolean existsByEmployeeId(Long employeeId);

    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isEnabled = true")
    Optional<User> findEnabledUserByUsername(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.username = :username")
    void incrementFailedLoginAttempts(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.isAccountLocked = false, " +
           "u.accountLockedUntil = NULL WHERE u.username = :username")
    void resetFailedLoginAttempts(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isAccountLocked = true, u.accountLockedUntil = :lockUntil WHERE u.username = :username")
    void lockAccount(@Param("username") String username, @Param("lockUntil") LocalDateTime lockUntil);

    @Query("SELECT u FROM User u WHERE u.isAccountLocked = true AND u.accountLockedUntil <= :now")
    List<User> findLockedUsersToUnlock(@Param("now") LocalDateTime now);
}