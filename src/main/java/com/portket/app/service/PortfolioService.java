package com.portket.app.service;

import com.portket.app.constant.ComponentType;
import com.portket.app.domain.*;
import com.portket.app.dto.*;
import com.portket.app.dto.http.DataResponse;
import com.portket.app.dto.http.ListResponse;
import com.portket.app.finder.InstrumentFinder;
import com.portket.app.finder.PortfolioFinder;
import com.portket.app.finder.TagFinder;
import com.portket.app.repository.PortfolioRepository;
import com.portket.exception.BizException;
import com.portket.util.system.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.portket.app.constant.ComponentType.INSTRUMENT;
import static com.portket.app.constant.ComponentType.TAG;
import com.portket.exception.ErrorCode;
import static com.portket.exception.ErrorCode.NOT_NULL;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioFinder portfolioFinder;
    private final InstrumentFinder instrumentFinder;
    private final TagFinder tagFinder;

    @Transactional
    public void createPortfolio(PortfolioInput input) {

        validTotalWeight(input.getComponents());

        User user = SecurityUtils.getCurrentUserOrThrow();

        validNameDup(input.getName());

        Portfolio portfolio = Portfolio.builder()
                .name(input.getName())
                .user(user)
                .build();

        addPortfolioComponent(input.getComponents(), portfolio, user);

        portfolioRepository.save(portfolio);
    }

    private void validNameDup(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new BizException("필수입력", NOT_NULL, "");
        }
        User user = SecurityUtils.getCurrentUserOrThrow();
        Optional<Portfolio> portfolio = portfolioRepository.findByUserAndName(user, name);
        if (portfolio.isPresent()) {
            throw new BizException(ErrorCode.DUPLICATE_PORTFOLIO_NAME, "포트폴리오 이름: " + name);
        }
    }

    @Transactional
    public void updateWeight(PortfolioInput input) {

        validTotalWeight(input.getComponents());

        User user = SecurityUtils.getCurrentUserOrThrow();

        Portfolio portfolio = portfolioFinder.validOwner(input.getName(), user);

        portfolio.getPortfolioInstruments().clear();
        portfolio.getPortfolioTags().clear();

        addPortfolioComponent(input.getComponents(), portfolio, user);
    }

    private void addPortfolioComponent(List<PortfolioComponentRequest> input, Portfolio portfolio, User user) {

        List<PortfolioTag> tags = new ArrayList<>();
        List<PortfolioInstrument> instruments = new ArrayList<>();

        input.forEach(
                component -> {

                    if (StringUtils.isEmpty(component.getComponentType().toString()) || !Arrays.asList(ComponentType.values()).contains(component.getComponentType())) {
                        throw new IllegalArgumentException("Not exist component type");
                    }
                    if (StringUtils.isEmpty(component.getName())) {
                        throw new IllegalArgumentException("Not exist component name");
                    }

                    if (TAG.equals(component.getComponentType())) {
                        Tag tag = tagFinder.validOwner(component.getName(), user);
                        PortfolioTag tagComponent = PortfolioTag.builder()
                                .portfolio(portfolio)
                                .tag(tag)
                                .weight(component.getWeight())
                                .build();
                        tags.add(tagComponent);
                    } else if (INSTRUMENT.equals(component.getComponentType())) {
                        Instrument instrument = instrumentFinder.validTicker(component.getName());

                        PortfolioInstrument instrumentComponent = PortfolioInstrument.builder()
                                .portfolio(portfolio)
                                .instrument(instrument)
                                .weight(component.getWeight())
                                .build();
                        instruments.add(instrumentComponent);
                    } else {
                        throw new IllegalArgumentException("Unknown component type: " + component.getComponentType());
                    }
                }
        );
        portfolio.getPortfolioInstruments().addAll(instruments);
        portfolio.getPortfolioTags().addAll(tags);
    }

    private static void validTotalWeight(List<PortfolioComponentRequest> input) {
        // 입력값 검증
        BigDecimal totalWeight = input.stream()
                .map(PortfolioComponentRequest::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (!totalWeight.equals(BigDecimal.valueOf(100))) {
            throw new IllegalArgumentException("Total weight must be 100");
        }
    }

    @Transactional
    public void deletePortfolio(String name) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        Portfolio portfolio = portfolioFinder.validOwner(name, user);
        portfolioRepository.delete(portfolio);
    }

    @Transactional(readOnly = true)
    public ListResponse<PortfolioOutput> listPortfolio(String name) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        List<PortfolioOutput> output = portfolioFinder.listPortfoliosWithName(name, user.getId());
        return ListResponse.<PortfolioOutput>builder().data(output).totalElements(output.size()).build();
    }

    @Transactional(readOnly = true)
    public DataResponse<PortfolioOutput> get(String name) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        PortfolioOutput data = portfolioFinder.get(name, user);
        return DataResponse.<PortfolioOutput>builder().data(data).inqYn(data != null).build();
    }
}
