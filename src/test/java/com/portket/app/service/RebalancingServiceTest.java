package com.portket.app.service;

import com.portket.app.domain.*;
import com.portket.app.dto.RebalancingInput;
import com.portket.app.dto.RebalancingOutput;
import com.portket.app.repository.HoldingRepository;
import com.portket.app.repository.PortfolioRepository;
import com.portket.exception.BizException;
import com.portket.util.system.SecurityUtils;
import com.portket.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RebalancingService 테스트")
class RebalancingServiceTest {
    
    @Mock
    private PortfolioRepository portfolioRepository;
    
    @Mock
    private HoldingRepository holdingRepository;
    
    @Mock
    private TransactionService transactionService;
    
    @InjectMocks
    private RebalancingService rebalancingService;
    
    private User testUser;
    private Portfolio testPortfolio;
    private Instrument appleStock;
    private Instrument googleStock;
    
    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createUser();
        testUser.setId(1L);
        
        testPortfolio = TestDataBuilder.createPortfolio(testUser);
        testPortfolio.setId(1L);
        
        appleStock = TestDataBuilder.createInstrument("AAPL", "Apple Inc.", new BigDecimal("150.00"));
        appleStock.setId(1L);
        
        googleStock = TestDataBuilder.createInstrument("GOOGL", "Google Inc.", new BigDecimal("2000.00"));
        googleStock.setId(2L);
    }
    
    @Test
    @DisplayName("리밸런싱 계산 성공")
    void calculateRebalancing_Success() {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(1L)
                .totalInvestmentAmount(new BigDecimal("10000.00"))
                .simulate(true)
                .build();
        
        // 포트폴리오 구성: AAPL 60%, GOOGL 40%
        PortfolioInstrument pi1 = TestDataBuilder.createPortfolioInstrument(
                testPortfolio, appleStock, new BigDecimal("60")
        );
        PortfolioInstrument pi2 = TestDataBuilder.createPortfolioInstrument(
                testPortfolio, googleStock, new BigDecimal("40")
        );
        testPortfolio.setPortfolioInstruments(Arrays.asList(pi1, pi2));
        
        // 현재 보유: AAPL 20주, GOOGL 1주
        Holding holding1 = TestDataBuilder.createHolding(
                testPortfolio, appleStock, new BigDecimal("20"), new BigDecimal("140.00")
        );
        Holding holding2 = TestDataBuilder.createHolding(
                testPortfolio, googleStock, new BigDecimal("1"), new BigDecimal("1900.00")
        );
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(holdingRepository.findByPortfolio(testPortfolio)).thenReturn(Arrays.asList(holding1, holding2));
            
            // when
            RebalancingOutput result = rebalancingService.calculateRebalancing(input);
            
            // then
            assertThat(result).isNotNull();
            assertThat(result.getPortfolioId()).isEqualTo(1L);
            assertThat(result.getTotalInvestmentAmount()).isEqualByComparingTo("10000.00");
            assertThat(result.isSimulated()).isTrue();
            assertThat(result.getItems()).hasSize(2);
            
            // AAPL 리밸런싱 검증
            RebalancingOutput.RebalancingItem appleItem = result.getItems().stream()
                    .filter(item -> item.getTicker().equals("AAPL"))
                    .findFirst()
                    .orElseThrow();
            assertThat(appleItem.getTargetWeight()).isEqualByComparingTo("60");
            assertThat(appleItem.getTargetValue()).isEqualByComparingTo("6000.00");
            assertThat(appleItem.getCurrentQuantity()).isEqualByComparingTo("20");
            assertThat(appleItem.getAction()).isEqualTo("BUY");
            
            // GOOGL 리밸런싱 검증
            RebalancingOutput.RebalancingItem googleItem = result.getItems().stream()
                    .filter(item -> item.getTicker().equals("GOOGL"))
                    .findFirst()
                    .orElseThrow();
            assertThat(googleItem.getTargetWeight()).isEqualByComparingTo("40");
            assertThat(googleItem.getTargetValue()).isEqualByComparingTo("4000.00");
            assertThat(googleItem.getCurrentQuantity()).isEqualByComparingTo("1");
            assertThat(googleItem.getAction()).isEqualTo("BUY");
        }
    }
    
    @Test
    @DisplayName("리밸런싱 계산 실패 - 포트폴리오 없음")
    void calculateRebalancing_PortfolioNotFound() {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(999L)
                .totalInvestmentAmount(new BigDecimal("10000.00"))
                .simulate(true)
                .build();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(portfolioRepository.findById(999L)).thenReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> rebalancingService.calculateRebalancing(input))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("포트폴리오를 찾을 수 없습니다");
        }
    }
    
    @Test
    @DisplayName("리밸런싱 계산 실패 - 권한 없음")
    void calculateRebalancing_Unauthorized() {
        // given
        User otherUser = TestDataBuilder.createUser("other@example.com", "Other User");
        otherUser.setId(2L);
        
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(1L)
                .totalInvestmentAmount(new BigDecimal("10000.00"))
                .simulate(true)
                .build();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(otherUser);
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            
            // when & then
            assertThatThrownBy(() -> rebalancingService.calculateRebalancing(input))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("해당 포트폴리오에 대한 권한이 없습니다");
        }
    }
    
    @Test
    @DisplayName("리밸런싱 계산 실패 - 포트폴리오 상품 없음")
    void calculateRebalancing_NoInstruments() {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(1L)
                .totalInvestmentAmount(new BigDecimal("10000.00"))
                .simulate(true)
                .build();
        
        testPortfolio.setPortfolioInstruments(Collections.emptyList());
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            
            // when & then
            assertThatThrownBy(() -> rebalancingService.calculateRebalancing(input))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("포트폴리오에 설정된 상품이 없습니다");
        }
    }
    
    @Test
    @DisplayName("리밸런싱 계산 실패 - 비중 합계 100% 아님")
    void calculateRebalancing_InvalidWeightSum() {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(1L)
                .totalInvestmentAmount(new BigDecimal("10000.00"))
                .simulate(true)
                .build();
        
        // 비중 합계가 90%인 경우
        PortfolioInstrument pi1 = TestDataBuilder.createPortfolioInstrument(
                testPortfolio, appleStock, new BigDecimal("50")
        );
        PortfolioInstrument pi2 = TestDataBuilder.createPortfolioInstrument(
                testPortfolio, googleStock, new BigDecimal("40")
        );
        testPortfolio.setPortfolioInstruments(Arrays.asList(pi1, pi2));
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            
            // when & then
            assertThatThrownBy(() -> rebalancingService.calculateRebalancing(input))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("포트폴리오의 목표 비중 합계가 100%가 아닙니다");
        }
    }
    
    @Test
    @DisplayName("리밸런싱 실행 성공")
    void executeRebalancing_Success() {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(1L)
                .totalInvestmentAmount(new BigDecimal("10000.00"))
                .simulate(false)
                .build();
        
        PortfolioInstrument pi1 = TestDataBuilder.createPortfolioInstrument(
                testPortfolio, appleStock, new BigDecimal("60")
        );
        PortfolioInstrument pi2 = TestDataBuilder.createPortfolioInstrument(
                testPortfolio, googleStock, new BigDecimal("40")
        );
        testPortfolio.setPortfolioInstruments(Arrays.asList(pi1, pi2));
        
        Holding holding1 = TestDataBuilder.createHolding(
                testPortfolio, appleStock, new BigDecimal("20"), new BigDecimal("140.00")
        );
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
            when(holdingRepository.findByPortfolio(testPortfolio)).thenReturn(Arrays.asList(holding1));
            
            // when
            RebalancingOutput result = rebalancingService.executeRebalancing(input);
            
            // then
            assertThat(result).isNotNull();
            assertThat(result.isSimulated()).isFalse();
            verify(transactionService, atLeastOnce()).save(any());
        }
    }
}