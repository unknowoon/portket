package com.portket.util;

import com.portket.app.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TestDataBuilder {
    
    public static User createUser() {
        return User.builder()
                .email("test@example.com")
                .name("Test User")
                .build();
    }
    
    public static User createUser(String email, String name) {
        return User.builder()
                .email(email)
                .name(name)
                .build();
    }
    
    public static Portfolio createPortfolio(User user) {
        return Portfolio.builder()
                .name("Test Portfolio")
                .user(user)
                .build();
    }
    
    public static Instrument createInstrument(String ticker, String name, BigDecimal price) {
        return Instrument.builder()
                .ticker(ticker)
                .name(name)
                .currentPrice(price)
                .currency("USD")
                .market("NASDAQ")
                .country("US")
                .build();
    }
    
    public static Transaction createTransaction(User user, Instrument instrument, String type, BigDecimal quantity, BigDecimal price) {
        return Transaction.builder()
                .user(user)
                .instrument(instrument)
                .transactionType(type)
                .quantity(quantity)
                .price(price)
                .amount(quantity.multiply(price))
                .transactionDate(LocalDate.now())
                .build();
    }
    
    public static Holding createHolding(Portfolio portfolio, Instrument instrument, BigDecimal quantity, BigDecimal avgPrice) {
        return Holding.builder()
                .portfolio(portfolio)
                .instrument(instrument)
                .quantity(quantity)
                .averagePrice(avgPrice)
                .build();
    }
    
    public static Balance createBalance(User user, Instrument instrument, BigDecimal quantity, BigDecimal avgPrice) {
        return Balance.builder()
                .user(user)
                .instrument(instrument)
                .quantity(quantity)
                .averagePrice(avgPrice)
                .build();
    }
    
    public static PortfolioInstrument createPortfolioInstrument(Portfolio portfolio, Instrument instrument, BigDecimal weight) {
        return PortfolioInstrument.builder()
                .portfolio(portfolio)
                .instrument(instrument)
                .weight(weight)
                .build();
    }
}