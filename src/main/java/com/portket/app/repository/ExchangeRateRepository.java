package com.portket.app.repository;

import com.portket.app.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findByCurrencyPairAndRateDate(String currencyPair, LocalDate rateDate);
    List<ExchangeRate> findByCurrencyPairInAndRateDate(Collection<String> currencyPairs, LocalDate rateDate);
}
