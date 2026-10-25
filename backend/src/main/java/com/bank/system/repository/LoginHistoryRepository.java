package com.bank.system.repository;

import com.bank.system.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    List<LoginHistory> findByUsernameOrderByLoginTimeDesc(String username);

    @Query("SELECT l FROM LoginHistory l WHERE l.username = :username ORDER BY l.loginTime DESC")
    Optional<LoginHistory> findLatestByUsername(@Param("username") String username);

    @Query("SELECT COUNT(l) FROM LoginHistory l WHERE l.username = :username AND l.status = 'FAILURE' AND l.loginTime >= :since")
    long countFailedAttemptsSince(@Param("username") String username, @Param("since") LocalDateTime since);

    @Query("SELECT l FROM LoginHistory l WHERE l.loginTime >= :since AND l.status = 'FAILURE'")
    List<LoginHistory> findRecentFailedAttempts(@Param("since") LocalDateTime since);
}