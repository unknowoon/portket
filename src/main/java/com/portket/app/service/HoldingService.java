package com.portket.app.service;

import com.portket.app.constant.Currency;
import com.portket.app.domain.*;
import com.portket.app.dto.HoldingInput;
import com.portket.app.dto.HoldingListInqInput;
import com.portket.app.dto.HoldingListInqOutput;
import com.portket.app.dto.HoldingListInquirySubOutput;
import com.portket.app.dto.record.EvaluatedHolding;
import com.portket.app.finder.HoldingFinder;
import com.portket.app.finder.InstrumentFinder;
import com.portket.app.finder.PortfolioFinder;
import com.portket.app.repository.ExchangeRateRepository;
import com.portket.app.repository.HoldingRepository;
import com.portket.util.finance.FinancialCalculator;
import com.portket.app.dto.record.UpdateResult;
import com.portket.util.system.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final InstrumentFinder instrumentFinder;
    private final PortfolioFinder portfolioFinder;
    private final HoldingRepository holdingRepository;
    private final HoldingFinder holdingFinder;
    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional(readOnly = true)
    public HoldingListInqOutput list(String name) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        Portfolio portfolio = portfolioFinder.validOwner(name, user);
        List<Holding> holdings = holdingFinder.getListByPortfolio(portfolio);

        // 1. 홀딩에서 사용된 통화 집합 구성 (평가 시 반드시 USD와 KRW 포함)
        Set<Currency> currencies = holdings.stream()
                .map(h -> h.getInstrument().getCurrency())
                .collect(Collectors.toSet());
        currencies.add(Currency.USD);
        currencies.add(Currency.KRW);

        LocalDate today = LocalDate.now();
        // 2. USD 환산용 통화쌍 생성 및 환율 데이터 조회
        List<String> usdCurrencyPairs = currencies.stream()
                .filter(c -> c != Currency.USD)
                .map(Currency.USD::buildCurrencyPair)
                .distinct()
                .collect(Collectors.toList());
        List<ExchangeRate> exchangeRates = exchangeRateRepository.findByCurrencyPairInAndRateDate(usdCurrencyPairs, today);
        Map<String, ExchangeRate> exchangeRateMap = exchangeRates.stream()
                .collect(Collectors.toMap(ExchangeRate::getCurrencyPair, Function.identity()));

        // 3. 각 홀딩에 대해 평가 데이터를 계산
        //    내부 클래스 HoldingEvaluation는 중간 결과를 저장하기 위한 용도입니다.

        List<EvaluatedHolding> evaluations = holdings.stream().map(h -> {
            Instrument instr = h.getInstrument();
            Currency curr = instr.getCurrency();
            BigDecimal quantity = h.getQuantity();
            BigDecimal currentPrice = instr.getCurrentPrice();
            BigDecimal averagePrice = h.getAveragePrice();
            BigDecimal totalAmount = quantity.multiply(currentPrice);

            // USD 평가: 만약 해당 통화가 USD이면 그대로, 아니면 환산
            BigDecimal evaluatedUsd;
            if (curr == Currency.USD) {
                evaluatedUsd = totalAmount;
            } else {
                ExchangeRate rate = exchangeRateMap.get(Currency.USD.buildCurrencyPair(curr));
                if (rate == null || rate.getClose().compareTo(BigDecimal.ZERO) == 0) {
                    evaluatedUsd = BigDecimal.ZERO;
                } else {
                    evaluatedUsd = (curr.getPriority() < Currency.USD.getPriority())
                            ? totalAmount.multiply(rate.getClose()).setScale(2, RoundingMode.HALF_UP)
                            : totalAmount.divide(rate.getClose(), 2, RoundingMode.HALF_UP);
                }
            }

            // KRW 평가: 해당 통화가 KRW이면 그대로, 아니면 USD 평가 후 USD-KRW 환율 적용
            BigDecimal evaluatedKrw;
            if (curr == Currency.KRW) {
                evaluatedKrw = totalAmount;
            } else {
                ExchangeRate usdKrw = exchangeRateRepository.findByCurrencyPairAndRateDate("USDKRW", today)
                        .orElse(null);
                if (usdKrw == null || usdKrw.getClose().compareTo(BigDecimal.ZERO) == 0) {
                    evaluatedKrw = BigDecimal.ZERO;
                } else {
                    evaluatedKrw = evaluatedUsd.multiply(usdKrw.getClose()).setScale(2, RoundingMode.HALF_UP);
                }
            }

            // 수익 계산
            BigDecimal profitAmount = currentPrice.subtract(averagePrice).multiply(quantity);
            BigDecimal profitRatio = BigDecimal.ZERO;
            if (averagePrice.compareTo(BigDecimal.ZERO) != 0) {
                profitRatio = currentPrice.divide(averagePrice, 4, RoundingMode.HALF_UP)
                        .subtract(BigDecimal.ONE)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            return new EvaluatedHolding(h, totalAmount, evaluatedUsd, evaluatedKrw, profitRatio, profitAmount);
        }).toList();

        // 4. 전체 포트폴리오의 USD 평가금액 합계 계산
        BigDecimal totalEvaluatedUsd = evaluations.stream()
                .map(EvaluatedHolding::evaluatedUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. 각 홀딩별 비중(weight) 계산 및 DTO 매핑
        List<HoldingListInquirySubOutput> subOutputs = evaluations.stream().map(e -> {
            BigDecimal weight = BigDecimal.ZERO;
            if (totalEvaluatedUsd.compareTo(BigDecimal.ZERO) > 0) {
                weight = e.evaluatedUsd().divide(totalEvaluatedUsd, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            return HoldingListInquirySubOutput.builder()
                    .name(e.holding().getInstrument().getName())
                    .ticker(e.holding().getInstrument().getTicker())
                    .country(e.holding().getInstrument().getCountry())
                    .market(e.holding().getInstrument().getMarket())
                    .currency(e.holding().getInstrument().getCurrency())
                    .currentPrice(e.holding().getInstrument().getCurrentPrice())
                    .quantity(e.holding().getQuantity())
                    .averagePrice(e.holding().getAveragePrice())
                    .totalAmount(e.totalAmount())
                    .evaluatedAmountUsd(e.evaluatedUsd())
                    .evaluatedAmountKrw(e.evaluatedKrw())
                    .profitRatio(e.profitRatio())
                    .profitAmount(e.profitAmount())
                    .weight(weight)
                    .build();
        }).toList();

        // 6. 전체 포트폴리오의 총 평가금액 및 수익 계산
        BigDecimal overallUsd = subOutputs.stream()
                .map(HoldingListInquirySubOutput::getEvaluatedAmountUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overallKrw = subOutputs.stream()
                .map(HoldingListInquirySubOutput::getEvaluatedAmountKrw)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overallProfitUsd = evaluations.stream()
                .map(EvaluatedHolding::profitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overallProfitRatio = BigDecimal.ZERO;
        if (overallUsd.compareTo(BigDecimal.ZERO) > 0) {
            overallProfitRatio = overallProfitUsd.divide(overallUsd, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return HoldingListInqOutput.builder()
                .portfolioName(portfolio.getName())
                .totalAmountKrw(overallKrw)
                .totalAmountUsd(overallUsd)
                .usdEvaluatedProfit(overallProfitUsd)
                .profitRatio(overallProfitRatio)
                .data(subOutputs)
                .build();
    }

    @Transactional
    public void save(HoldingInput input) {

        User user = SecurityUtils.getCurrentUserOrThrow();
        Portfolio portfolio = portfolioFinder.validOwner(input.getPortfolioName(), user);
        Instrument instrument = instrumentFinder.validTicker(input.getTicker());

        Holding holding = holdingFinder.getHoldingOrNullByPortfolioAndInstrument(portfolio, instrument);

        if (holding == null) {
            holding = Holding.builder()
                    .portfolio(portfolio)
                    .instrument(instrument)
                    .quantity(input.getQuantity())
                    .averagePrice(input.getAveragePrice())
                    .weight(input.getWeight())
                    .build();

            holdingRepository.save(holding);
        } else {
            UpdateResult result = FinancialCalculator.updateBalance(
                    holding.getQuantity(), holding.getAveragePrice(),
                    input.getQuantity(), input.getAveragePrice(),
                    input.getTransactionType()
            );

            holding.changeQuantityAndAveragePrice(result.quantity(), result.averagePrice());
        }

        recalculateWeight(portfolio);
    }

    /**
     * 포트폴리오별 홀딩 종목 비중 재계산 로직이다.
     * 홀딩은 포트폴리오의 "현재" 상태를 기록하는 엔티티라 주기적으로 배치를 돌려야 한다.
     * @param portfolio
     */
    @Transactional
    public void recalculateWeight(Portfolio portfolio) {

        List<Holding> holdings = holdingFinder.getListByPortfolio(portfolio);

        // 1. 전체 금액 계산
        // NOTE 복잡하게 썼지만 요컨대 holding에서 가져온 instrument나 currentPrice가 널이더라도 ZERO를 가져오는 일입니다
        BigDecimal totalValue = holdings.stream()
                .map(holding -> holding.getQuantity().multiply(
                        Optional.ofNullable(holding.getInstrument())
                                .map(Instrument::getCurrentPrice)
                        .orElse(BigDecimal.ZERO)
                        )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. 홀딩스 내를 루프 돌면서
        for (Holding holding : holdings) {
            BigDecimal price = Optional.ofNullable(holding.getInstrument()).map(Instrument::getCurrentPrice).orElse(BigDecimal.ZERO);
            BigDecimal holdingValue = holding.getQuantity().multiply(price);

            BigDecimal weight = BigDecimal.ZERO;
            if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                weight = holdingValue.multiply(BigDecimal.valueOf(100))
                        .divide(totalValue, 2, RoundingMode.HALF_UP);
            }

            holding.changeWeight(weight);
        }
    }
}
