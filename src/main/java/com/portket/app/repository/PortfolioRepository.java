package com.portket.app.repository;

import com.portket.app.domain.Portfolio;
import com.portket.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Portfolio findByName(String name);                              // 중복 검증
    Optional<Portfolio> findByUserAndName(User user, String name);  // 소유 검증
    List<Portfolio> findByUser(User user);                          // 소유자 전체 조회
}
