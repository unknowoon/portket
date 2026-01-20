package com.portket.app.repository;

import com.portket.app.domain.Holding;
import com.portket.app.domain.Instrument;
import com.portket.app.domain.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByPortfolioAndInstrument(Portfolio portfolio, Instrument instrument);
    List<Holding> findByPortfolioInAndInstrument(List<Portfolio> portfolios, Instrument instrument);
    List<Holding> findByPortfolio(Portfolio portfolio);
}
