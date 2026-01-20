package com.portket.app.service;

import com.portket.app.domain.Balance;
import com.portket.app.domain.Instrument;
import com.portket.app.domain.User;
import com.portket.app.dto.BalanceListInquiryInput;
import com.portket.app.dto.BalanceListInquiryOutput;
import com.portket.app.dto.BalanceSaveInput;
import com.portket.app.finder.BalanceFinder;
import com.portket.app.finder.InstrumentFinder;
import com.portket.app.repository.BalanceRepository;
import com.portket.exception.BizException;
import com.portket.exception.ErrorCode;
import com.portket.util.finance.FinancialCalculator;
import com.portket.app.dto.record.UpdateResult;
import com.portket.util.system.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final BalanceFinder balanceFinder;
    private final InstrumentFinder instrumentFinder;

    @Transactional
    public void save(BalanceSaveInput input) {

        User user = SecurityUtils.getCurrentUserOrThrow();
        Instrument instrument = instrumentFinder.validTicker(input.getTicker());

        Balance balance = balanceFinder.getBalanceOrNull(instrument, user);

        if (balance == null) {
            balance = Balance.builder()
                    .user(user)
                    .instrument(instrument)
                    .quantity(input.getQuantity())
                    .averagePrice(input.getAveragePrice())
                    .build();
            balanceRepository.save(balance);
        } else {
            UpdateResult result = FinancialCalculator.updateBalance(
                    balance.getQuantity(), balance.getAveragePrice(),
                    input.getQuantity(), input.getAveragePrice(),
                    input.getTransactionType()
            );

            balance.changeQuantityAndAveragePrice(result.quantity(), result.averagePrice());
        }
    }

    @Transactional
    public void create(BalanceSaveInput input) {

        User user = SecurityUtils.getCurrentUserOrThrow();
        Instrument instrument = instrumentFinder.validTicker(input.getTicker());
        Balance balance = balanceFinder.getBalanceOrNull(instrument, user);

        if (balance == null) {
            save(input);
        } else {
            throw new BizException("Balance already exists for this instrument", ErrorCode.INVALID_REQUEST, 
                    String.format("ticker: %s", input.getTicker()));
        }
    }

    @Transactional(readOnly = true)
    public BalanceListInquiryOutput list(BalanceListInquiryInput input) {
        return balanceFinder.list(input);
    }
}
