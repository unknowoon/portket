package com.portket.app.finder;

import com.portket.app.domain.Portfolio;
import com.portket.app.domain.User;
import com.portket.app.dto.Component;
import com.portket.app.dto.PortfolioOutput;
import com.portket.app.repository.PortfolioRepository;
import com.portket.app.repository.jooq.PortfolioJooqQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.portket.app.constant.ComponentType.INSTRUMENT;
import static com.portket.app.constant.ComponentType.TAG;

@Service
@RequiredArgsConstructor
public class PortfolioFinder {
    private final PortfolioJooqQuery jooqQuery;
    private final PortfolioRepository portfolioRepository;

    public PortfolioOutput get(String name, User user) {
        Portfolio portfolio = portfolioRepository.findByUserAndName(user, name)
                .orElse(null);

        if (portfolio == null) {
            return null;
        }

        List<Component> components = new ArrayList<>();

        List<Component> tags = portfolio.getPortfolioTags().stream()
                .map(c -> Component.builder()
                        .componentType(TAG)
                        .name(c.getTag().getName())
                        .weight(c.getWeight())
                        .build())
                .toList();

        List<Component> insts = portfolio.getPortfolioInstruments().stream()
                .map(c -> Component.builder()
                        .componentType(INSTRUMENT)
                        .name(c.getInstrument().getName())
                        .weight(c.getWeight())
                        .build())
                .toList();

        components.addAll(tags);
        components.addAll(insts);

        return PortfolioOutput.builder()
                .id(portfolio.getId())
                .name(portfolio.getName())
                .components(components)
                .build();
    }

    public Portfolio validOwner(String name, User user) {
        return portfolioRepository.findByUserAndName(user, name)
                .orElseThrow(() -> new IllegalArgumentException("Not exist portfolio"));
    }

    public List<PortfolioOutput> listPortfoliosWithName(String name, Long userId) {
        return jooqQuery.listPortfoliosWithName(name, userId);
    }

    public List<Portfolio> getListOwn(User user) {
        return portfolioRepository.findByUser(user);
    }
}
