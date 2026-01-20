package com.portket.integration;

import com.portket.app.domain.*;
import com.portket.app.dto.RebalancingInput;
import com.portket.app.dto.RebalancingOutput;
import com.portket.app.repository.*;
import com.portket.app.service.RebalancingService;
import com.portket.util.TestDataBuilder;
import com.portket.util.system.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@Transactional
@DisplayName("리밸런싱 통합 테스트")
class RebalancingIntegrationTest {
    
    @Autowired
    private RebalancingService rebalancingService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PortfolioRepository portfolioRepository;
    
    @Autowired
    private InstrumentRepository instrumentRepository;
    
    @Autowired
    private HoldingRepository holdingRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    private User testUser;
    private Portfolio testPortfolio;
    private Instrument appleStock;
    private Instrument googleStock;
    private Instrument msftStock;
    
    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = userRepository.save(TestDataBuilder.createUser());
        
        // 포트폴리오 생성
        testPortfolio = portfolioRepository.save(TestDataBuilder.createPortfolio(testUser));
        
        // 금융 상품 생성
        appleStock = instrumentRepository.save(
                TestDataBuilder.createInstrument("AAPL", "Apple Inc.", new BigDecimal("150.00"))
        );
        googleStock = instrumentRepository.save(
                TestDataBuilder.createInstrument("GOOGL", "Google Inc.", new BigDecimal("2000.00"))
        );
        msftStock = instrumentRepository.save(
                TestDataBuilder.createInstrument("MSFT", "Microsoft Corp.", new BigDecimal("300.00"))
        );
        
        // 포트폴리오 구성 설정 (AAPL 50%, GOOGL 30%, MSFT 20%)
        PortfolioInstrument pi1 = TestDataBuilder.createPortfolioInstrument(
                testPortfolio, appleStock, new BigDecimal("50")
        );
        PortfolioInstrument pi2 = TestDataBuilder.createPortfolioInstrument(
                testPortfolio, googleStock, new BigDecimal("30")
        );
        PortfolioInstrument pi3 = TestDataBuilder.createPortfolioInstrument(
                testPortfolio, msftStock, new BigDecimal("20")
        );
        testPortfolio.getPortfolioInstruments().addAll(List.of(pi1, pi2, pi3));
        portfolioRepository.save(testPortfolio);
        
        // 현재 보유 자산 설정
        holdingRepository.save(TestDataBuilder.createHolding(
                testPortfolio, appleStock, new BigDecimal("10"), new BigDecimal("140.00")
        ));
        holdingRepository.save(TestDataBuilder.createHolding(
                testPortfolio, googleStock, new BigDecimal("1"), new BigDecimal("1900.00")
        ));
        // MSFT는 보유하지 않음
    }
    
    @Test
    @DisplayName("리밸런싱 시뮬레이션 - 전체 시나리오")
    void rebalancingSimulation_FullScenario() {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(testPortfolio.getId())
                .totalInvestmentAmount(new BigDecimal("50000.00"))
                .simulate(true)
                .build();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            
            // when
            RebalancingOutput result = rebalancingService.calculateRebalancing(input);
            
            // then
            assertThat(result).isNotNull();
            assertThat(result.getPortfolioId()).isEqualTo(testPortfolio.getId());
            assertThat(result.getTotalInvestmentAmount()).isEqualByComparingTo("50000.00");
            assertThat(result.isSimulated()).isTrue();
            assertThat(result.getItems()).hasSize(3);
            
            // AAPL 검증 (목표: 50% = 25,000)
            RebalancingOutput.RebalancingItem appleItem = findItemByTicker(result, "AAPL");
            assertThat(appleItem.getTargetWeight()).isEqualByComparingTo("50");
            assertThat(appleItem.getTargetValue()).isEqualByComparingTo("25000.00");
            assertThat(appleItem.getCurrentQuantity()).isEqualByComparingTo("10");
            assertThat(appleItem.getCurrentValue()).isEqualByComparingTo("1500.00"); // 10 * 150
            assertThat(appleItem.getAction()).isEqualTo("BUY");
            
            // GOOGL 검증 (목표: 30% = 15,000)
            RebalancingOutput.RebalancingItem googleItem = findItemByTicker(result, "GOOGL");
            assertThat(googleItem.getTargetWeight()).isEqualByComparingTo("30");
            assertThat(googleItem.getTargetValue()).isEqualByComparingTo("15000.00");
            assertThat(googleItem.getCurrentQuantity()).isEqualByComparingTo("1");
            assertThat(googleItem.getCurrentValue()).isEqualByComparingTo("2000.00"); // 1 * 2000
            assertThat(googleItem.getAction()).isEqualTo("BUY");
            
            // MSFT 검증 (목표: 20% = 10,000)
            RebalancingOutput.RebalancingItem msftItem = findItemByTicker(result, "MSFT");
            assertThat(msftItem.getTargetWeight()).isEqualByComparingTo("20");
            assertThat(msftItem.getTargetValue()).isEqualByComparingTo("10000.00");
            assertThat(msftItem.getCurrentQuantity()).isEqualByComparingTo("0");
            assertThat(msftItem.getCurrentValue()).isEqualByComparingTo("0");
            assertThat(msftItem.getAction()).isEqualTo("BUY");
            
            // 총 비용 검증
            assertThat(result.getTotalCost()).isGreaterThan(BigDecimal.ZERO);
        }
    }
    
    @Test
    @DisplayName("리밸런싱 실행 - 거래 생성 확인")
    void rebalancingExecution_TransactionCreated() {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(testPortfolio.getId())
                .totalInvestmentAmount(new BigDecimal("10000.00"))
                .simulate(false)
                .build();
        
        long initialTransactionCount = transactionRepository.count();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            
            // when
            RebalancingOutput result = rebalancingService.executeRebalancing(input);
            
            // then
            assertThat(result.isSimulated()).isFalse();
            
            // 거래가 생성되었는지 확인
            long finalTransactionCount = transactionRepository.count();
            assertThat(finalTransactionCount).isGreaterThan(initialTransactionCount);
            
            // 생성된 거래 확인
            List<Transaction> newTransactions = transactionRepository.findAll()
                    .stream()
                    .filter(t -> "리밸런싱 자동 거래".equals(t.getMemo()))
                    .toList();
            
            assertThat(newTransactions).isNotEmpty();
            
            // 보유 자산이 업데이트되었는지 확인
            List<Holding> updatedHoldings = holdingRepository.findByPortfolio(testPortfolio);
            assertThat(updatedHoldings).isNotEmpty();
        }
    }
    
    @Test
    @DisplayName("리밸런싱 - 매도가 필요한 경우")
    void rebalancing_WithSellAction() {
        // given - AAPL을 과도하게 보유한 상황 설정
        Holding appleHolding = holdingRepository.findByPortfolioAndInstrument(testPortfolio, appleStock)
                .orElseThrow();
        appleHolding.setQuantity(new BigDecimal("200")); // 과도한 보유
        holdingRepository.save(appleHolding);
        
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(testPortfolio.getId())
                .totalInvestmentAmount(new BigDecimal("10000.00"))
                .simulate(true)
                .build();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            
            // when
            RebalancingOutput result = rebalancingService.calculateRebalancing(input);
            
            // then
            RebalancingOutput.RebalancingItem appleItem = findItemByTicker(result, "AAPL");
            assertThat(appleItem.getAction()).isEqualTo("SELL");
            assertThat(appleItem.getRequiredQuantity()).isGreaterThan(BigDecimal.ZERO);
        }
    }
    
    private RebalancingOutput.RebalancingItem findItemByTicker(RebalancingOutput output, String ticker) {
        return output.getItems().stream()
                .filter(item -> ticker.equals(item.getTicker()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Item not found for ticker: " + ticker));
    }
}