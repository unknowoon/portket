package com.portket.app.service;

import com.portket.app.domain.Instrument;
import com.portket.app.domain.Portfolio;
import com.portket.app.domain.Transaction;
import com.portket.app.domain.User;
import com.portket.app.dto.*;
import com.portket.app.dto.http.PaginatedResponse;
import com.portket.app.finder.InstrumentFinder;
import com.portket.app.finder.PortfolioFinder;
import com.portket.app.finder.TransactionFinder;
import com.portket.app.repository.TransactionRepository;
import com.portket.util.finance.FinancialCalculator;
import com.portket.util.system.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionFinder transactionFinder;
    private final TransactionRepository transactionRepository;
    private final InstrumentFinder instrumentFinder;
    private final PortfolioFinder portfolioFinder;
    private final HoldingService holdingService;
    private final BalanceService balanceService;

    @Transactional
    public void save(TransactionInput input) {
        User user = SecurityUtils.getCurrentUserOrThrow();

        // null 체크
        Objects.requireNonNull(input.getTransactionType(), "TransactionType is required");

        Portfolio portfolio = portfolioFinder.validOwner(input.getPortfolioName(), user);
        Instrument instrument = instrumentFinder.validTicker(input.getTicker());

        Transaction transaction = Transaction.builder()
                .user(user)
                .instrument(instrument)
                .transactionType(input.getTransactionType())
                .quantity(input.getQuantity())
                .amount(input.getAmount())
                .transactionDate(input.getTransactionDate())
                .build();

        transactionRepository.save(transaction);

        BigDecimal averagePrice = FinancialCalculator.calculateAveragePrice(input.getAmount(), input.getQuantity());

        HoldingInput holdingInput = HoldingInput.builder()
                .transactionType(input.getTransactionType())
                .portfolioName(portfolio.getName())
                .ticker(input.getTicker())
                .quantity(input.getQuantity())
                .averagePrice(averagePrice)
                .build();
        holdingService.save(holdingInput);

        BalanceSaveInput balanceSaveInput = BalanceSaveInput.builder()
                .transactionType(input.getTransactionType())
                .ticker(input.getTicker())
                .quantity(input.getQuantity())
                .averagePrice(averagePrice)
                .build();
        balanceService.save(balanceSaveInput);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<TransactionListInquiryOutput> list(TransactionListInquiryInput input) {
        User user = SecurityUtils.getCurrentUserOrThrow();
        return transactionFinder.list(input, user.getId());
    }
}
