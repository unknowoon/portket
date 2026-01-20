package com.portket.app.finder;

import com.portket.app.constant.Currency;
import com.portket.app.domain.Balance;
import com.portket.app.domain.ExchangeRate;
import com.portket.app.domain.Instrument;
import com.portket.app.domain.User;
import com.portket.app.dto.BalanceListInquiryInput;
import com.portket.app.dto.BalanceListInquiryOutput;
import com.portket.app.dto.BalanceListInquirySubOutput;
import com.portket.app.dto.record.EvaluatedBalance;
import com.portket.app.repository.BalanceRepository;
import com.portket.app.repository.ExchangeRateRepository;
import com.portket.util.system.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceFinder {
    private final BalanceRepository balanceRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public Balance getBalanceOrNull(Instrument instrument, User user) {
        return balanceRepository.findByInstrumentAndUser(instrument, user).orElse(null);
    }

    public BalanceListInquiryOutput list(BalanceListInquiryInput input) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        List<Balance> balances = balanceRepository.findByUser(user);

        Set<Currency> currencies = balances.stream()
                .map(b -> b.getInstrument().getCurrency())
                .collect(Collectors.toSet());
        currencies.add(Currency.USD);
        currencies.add(Currency.KRW);

        LocalDate today = LocalDate.now();
        Map<String, ExchangeRate> exchangeRateMap = getExchangeRateMap(currencies, today);

        List<EvaluatedBalance> evaluatedBalances = evaluateBalances(balances, exchangeRateMap);

        List<BalanceListInquirySubOutput> data = evaluatedBalances.stream()
                .map(eb -> {
                    Balance balance = eb.balance();
                    Instrument instrument = balance.getInstrument();
                    return BalanceListInquirySubOutput.builder()
                            .name(instrument.getName())
                            .ticker(instrument.getTicker())
                            .country(instrument.getCountry())
                            .market(instrument.getMarket())
                            .currency(instrument.getCurrency())
                            .currentPrice(instrument.getCurrentPrice())
                            .quantity(balance.getQuantity())
                            .averagePrice(balance.getAveragePrice())
                            .weight(eb.weight())
                            .totalAmount(eb.totalAmount())
                            .evaluatedAmountUsd(eb.evaluatedAmountUsd())
                            .evaluatedAmountKrw(eb.evaluatedAmountKrw())
                            .profitRatio(eb.profitRatio())
                            .profitAmount(eb.profitAmount())
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal krwTotal = data.stream()
                .map(BalanceListInquirySubOutput::getEvaluatedAmountKrw)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal usdTotal = data.stream()
                .map(BalanceListInquirySubOutput::getEvaluatedAmountUsd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUsdProfit = evaluatedBalances.stream()
                .map(EvaluatedBalance::profitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal overallProfitRatio = BigDecimal.ZERO;
        if (usdTotal.compareTo(BigDecimal.ZERO) > 0) {
            overallProfitRatio = totalUsdProfit.divide(usdTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return BalanceListInquiryOutput.builder()
                .totalAmountKrw(krwTotal)
                .totalAmountUsd(usdTotal)
                .usdEvaluatedProfit(totalUsdProfit)
                .profitRatio(overallProfitRatio)
                .data(data)
                .build();
    }

    /**
     * Helper method to build an exchange rate map for the given currencies and date.
     * It constructs USD-based currency pairs (excluding USD itself) and retrieves corresponding rates.
     */
    private Map<String, ExchangeRate> getExchangeRateMap(Set<Currency> currencies, LocalDate date) {
        List<String> usdCurrencyPairs = currencies.stream()
                .filter(currency -> currency != Currency.USD)
                .map(currency -> Currency.USD.buildCurrencyPair(currency))
                .distinct()
                .collect(Collectors.toList());
        List<ExchangeRate> exchangeRates = exchangeRateRepository.findByCurrencyPairInAndRateDate(usdCurrencyPairs, date);
        return exchangeRates.stream()
                .collect(Collectors.toMap(ExchangeRate::getCurrencyPair, Function.identity()));
    }

    /**
     * Evaluates a list of Balance objects to produce detailed EvaluatedBalance records.
     * This method calculates the total asset amount, USD/KRW evaluated amounts, profit ratio, profit amount,
     * and computes the asset weight based on total USD evaluation.
     *
     * @param balances         List of Balance objects
     * @param exchangeRateMap  Precomputed exchange rate map
     * @return List of EvaluatedBalance records
     */
    private List<EvaluatedBalance> evaluateBalances(List<Balance> balances, Map<String, ExchangeRate> exchangeRateMap) {
        // First, compute the total USD evaluated amount for the portfolio
        BigDecimal totalEvaluatedUsd = balances.stream()
                .map(balance -> {
                    Instrument instrument = balance.getInstrument();
                    Currency currency = instrument.getCurrency();
                    BigDecimal totalAmount = balance.getQuantity().multiply(instrument.getCurrentPrice());
                    if (currency == Currency.USD) {
                        return totalAmount;
                    } else {
                        ExchangeRate rate = exchangeRateMap.get(currency.buildCurrencyPair(Currency.USD));
                        if (rate == null || rate.getClose().compareTo(BigDecimal.ZERO) == 0) {
                            return BigDecimal.ZERO;
                        }
                        return (currency.getPriority() < Currency.USD.getPriority())
                                ? totalAmount.multiply(rate.getClose()).setScale(2, RoundingMode.HALF_UP)
                                : totalAmount.divide(rate.getClose(), 2, RoundingMode.HALF_UP);
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Map each Balance to an EvaluatedBalance record with computed weight
        return balances.stream().map(balance -> {
            Instrument instrument = balance.getInstrument();
            Currency currency = instrument.getCurrency();
            BigDecimal quantity = balance.getQuantity();
            BigDecimal currentPrice = instrument.getCurrentPrice();
            BigDecimal averagePrice = balance.getAveragePrice();
            BigDecimal totalAmount = quantity.multiply(currentPrice);

            BigDecimal evaluatedUsd = (currency == Currency.USD)
                    ? totalAmount
                    : convertValue(totalAmount, currency, Currency.USD, exchangeRateMap.get(currency.buildCurrencyPair(Currency.USD)));
            BigDecimal evaluatedKrw = (currency == Currency.KRW)
                    ? totalAmount
                    : convertValue(totalAmount, currency, Currency.KRW, exchangeRateMap.get(currency.buildCurrencyPair(Currency.USD)));

            BigDecimal profitAmount = currentPrice.subtract(averagePrice).multiply(quantity);
            BigDecimal profitRatio = BigDecimal.ZERO;
            if (averagePrice.compareTo(BigDecimal.ZERO) != 0) {
                profitRatio = currentPrice.divide(averagePrice, 4, RoundingMode.HALF_UP)
                        .subtract(BigDecimal.ONE)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal weight = BigDecimal.ZERO;
            if (totalEvaluatedUsd.compareTo(BigDecimal.ZERO) > 0) {
                weight = evaluatedUsd.divide(totalEvaluatedUsd, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            return new EvaluatedBalance(
                    balance,
                    weight,
                    totalAmount,
                    evaluatedUsd,
                    evaluatedKrw,
                    profitRatio,
                    profitAmount
            );
        }).collect(Collectors.toList());
    }

    /**
     * Converts a given amount from one currency to a target currency using the provided exchange rate.
     * For USD conversion, it either multiplies or divides based on the currency's priority relative to USD.
     * For KRW conversion, it first converts to USD then applies the USD-KRW rate.
     */
    private BigDecimal convertValue(BigDecimal amount, Currency from, Currency target, ExchangeRate exchangeRate) {
        if (from == target) {
            return amount;
        }
        if (target == Currency.USD) {
            if (exchangeRate == null || exchangeRate.getClose().compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return (from.getPriority() < Currency.USD.getPriority())
                    ? amount.multiply(exchangeRate.getClose()).setScale(2, RoundingMode.HALF_UP)
                    : amount.divide(exchangeRate.getClose(), 2, RoundingMode.HALF_UP);
        } else if (target == Currency.KRW) {
            BigDecimal usdValue = convertValue(amount, from, Currency.USD, exchangeRate);
            ExchangeRate usdKrw = exchangeRateRepository.findByCurrencyPairAndRateDate("USDKRW", LocalDate.now()).orElse(null);
            if (usdKrw == null || usdKrw.getClose().compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return usdValue.multiply(usdKrw.getClose()).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
