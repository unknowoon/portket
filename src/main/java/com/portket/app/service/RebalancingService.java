package com.portket.app.service;

import com.portket.app.domain.*;
import com.portket.app.dto.RebalancingInput;
import com.portket.app.dto.RebalancingOutput;
import com.portket.app.dto.TransactionInput;
import com.portket.app.repository.HoldingRepository;
import com.portket.app.repository.PortfolioRepository;
import com.portket.exception.BizException;
import com.portket.exception.ErrorCode;
import com.portket.util.system.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RebalancingService {
    
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final TransactionService transactionService;
    
    @Transactional(readOnly = true)
    public RebalancingOutput calculateRebalancing(RebalancingInput input) {
        User currentUser = SecurityUtils.getCurrentUserOrThrow();
        
        Portfolio portfolio = portfolioRepository.findById(input.getPortfolioId())
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "포트폴리오를 찾을 수 없습니다."));
        
        // 사용자 권한 확인
        if (!portfolio.getUser().getId().equals(currentUser.getId())) {
            throw new BizException(ErrorCode.INVALID_REQUEST, "해당 포트폴리오에 대한 권한이 없습니다.");
        }
        
        // 포트폴리오의 목표 비중 확인
        List<PortfolioInstrument> portfolioInstruments = portfolio.getPortfolioInstruments();
        if (portfolioInstruments.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_REQUEST, "포트폴리오에 설정된 상품이 없습니다.");
        }
        
        // 목표 비중 합계 검증 (100%인지 확인)
        BigDecimal totalWeight = portfolioInstruments.stream()
                .map(PortfolioInstrument::getWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalWeight.compareTo(new BigDecimal("100")) != 0) {
            throw new BizException(ErrorCode.INVALID_REQUEST, "포트폴리오의 목표 비중 합계가 100%가 아닙니다.");
        }
        
        // 현재 보유 현황 조회
        List<Holding> holdings = holdingRepository.findByPortfolio(portfolio);
        
        // 리밸런싱 계산
        List<RebalancingOutput.RebalancingItem> items = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (PortfolioInstrument pi : portfolioInstruments) {
            Instrument instrument = pi.getInstrument();
            BigDecimal targetWeight = pi.getWeight();
            BigDecimal targetValue = input.getTotalInvestmentAmount()
                    .multiply(targetWeight)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
            // 현재 보유 현황 찾기
            Holding holding = holdings.stream()
                    .filter(h -> h.getInstrument().getId().equals(instrument.getId()))
                    .findFirst()
                    .orElse(null);
            
            BigDecimal currentQuantity = holding != null ? holding.getQuantity() : BigDecimal.ZERO;
            BigDecimal currentPrice = instrument.getCurrentPrice();
            BigDecimal currentValue = currentQuantity.multiply(currentPrice);
            BigDecimal currentWeight = input.getTotalInvestmentAmount().compareTo(BigDecimal.ZERO) > 0
                    ? currentValue.multiply(new BigDecimal("100"))
                        .divide(input.getTotalInvestmentAmount(), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            
            // 필요 수량 계산
            BigDecimal targetQuantity = currentPrice.compareTo(BigDecimal.ZERO) > 0
                    ? targetValue.divide(currentPrice, 0, RoundingMode.DOWN)
                    : BigDecimal.ZERO;
            BigDecimal requiredQuantity = targetQuantity.subtract(currentQuantity);
            BigDecimal requiredValue = requiredQuantity.multiply(currentPrice);
            
            String action;
            if (requiredQuantity.compareTo(BigDecimal.ZERO) > 0) {
                action = "BUY";
                totalCost = totalCost.add(requiredValue);
            } else if (requiredQuantity.compareTo(BigDecimal.ZERO) < 0) {
                action = "SELL";
            } else {
                action = "HOLD";
            }
            
            items.add(RebalancingOutput.RebalancingItem.builder()
                    .instrumentId(instrument.getId())
                    .ticker(instrument.getTicker())
                    .instrumentName(instrument.getName())
                    .targetWeight(targetWeight)
                    .currentWeight(currentWeight)
                    .targetValue(targetValue)
                    .currentValue(currentValue)
                    .currentQuantity(currentQuantity)
                    .currentPrice(currentPrice)
                    .requiredQuantity(requiredQuantity.abs())
                    .requiredValue(requiredValue.abs())
                    .action(action)
                    .build());
        }
        
        return RebalancingOutput.builder()
                .portfolioId(portfolio.getId())
                .portfolioName(portfolio.getName())
                .totalInvestmentAmount(input.getTotalInvestmentAmount())
                .items(items)
                .totalCost(totalCost)
                .simulated(true)
                .build();
    }
    
    @Transactional
    public RebalancingOutput executeRebalancing(RebalancingInput input) {
        if (input.isSimulate()) {
            return calculateRebalancing(input);
        }
        
        // 리밸런싱 계산
        RebalancingOutput simulation = calculateRebalancing(input);
        
        // 실제 거래 실행
        for (RebalancingOutput.RebalancingItem item : simulation.getItems()) {
            if ("HOLD".equals(item.getAction())) {
                continue;
            }
            
            TransactionInput transactionInput = TransactionInput.builder()
                    .instrumentId(item.getInstrumentId())
                    .ticker(item.getTicker())
                    .portfolioId(simulation.getPortfolioId())
                    .transactionType("BUY".equals(item.getAction()) ? 
                        com.portket.app.constant.TransactionType.BUY : 
                        com.portket.app.constant.TransactionType.SELL)
                    .quantity(item.getRequiredQuantity())
                    .price(item.getCurrentPrice())
                    .amount(item.getRequiredValue())
                    .transactionDate(LocalDate.now())
                    .memo("리밸런싱 자동 거래")
                    .build();
            
            try {
                transactionService.save(transactionInput);
                log.info("리밸런싱 거래 실행: {} {} {} 주", 
                    item.getAction(), item.getTicker(), item.getRequiredQuantity());
            } catch (Exception e) {
                log.error("리밸런싱 거래 실패: {} {} {} 주", 
                    item.getAction(), item.getTicker(), item.getRequiredQuantity(), e);
                throw new BizException(ErrorCode.INTERNAL_SERVER_ERROR, 
                    String.format("%s %s 거래 중 오류가 발생했습니다.", item.getTicker(), item.getAction()));
            }
        }
        
        simulation.setSimulated(false);
        return simulation;
    }
}