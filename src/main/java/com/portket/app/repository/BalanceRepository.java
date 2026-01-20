package com.portket.app.repository;

import com.portket.app.domain.Balance;
import com.portket.app.domain.Instrument;
import com.portket.app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> findByInstrumentAndUser(Instrument instrument, User user);
    List<Balance> findByUser(User user);
}
