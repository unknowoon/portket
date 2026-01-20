package com.portket.app.finder;

import com.portket.app.domain.Holding;
import com.portket.app.domain.Instrument;
import com.portket.app.domain.Portfolio;
import com.portket.app.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HoldingFinder {
    private final HoldingRepository holdingRepository;

    public Holding getHoldingOrNullByPortfolioAndInstrument(Portfolio portfolio, Instrument instrument) {
        return holdingRepository.findByPortfolioAndInstrument(portfolio, instrument).orElse(null);
    }

    public Holding validOwner(Portfolio portfolio, Instrument instrument) {
        return holdingRepository.findByPortfolioAndInstrument(portfolio, instrument)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found"));
    }

    public List<Holding> getListByPortfolioInAndInstrument(List<Portfolio> portfolios, Instrument instrument) {
        return holdingRepository.findByPortfolioInAndInstrument(portfolios, instrument);
    }

    public List<Holding> getListByPortfolio(Portfolio portfolio) {
        return holdingRepository.findByPortfolio(portfolio);
    }
}
