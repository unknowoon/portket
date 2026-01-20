package com.portket.app.service;

import com.portket.app.constant.TransactionType;
import com.portket.app.domain.*;
import com.portket.app.dto.TransactionInput;
import com.portket.app.finder.InstrumentFinder;
import com.portket.app.finder.PortfolioFinder;
import com.portket.app.finder.TransactionFinder;
import com.portket.app.repository.TransactionRepository;
import com.portket.exception.BizException;
import com.portket.security.SecurityUtils;
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
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService 테스트")
class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private TransactionFinder transactionFinder;
    
    @Mock
    private InstrumentFinder instrumentFinder;
    
    @Mock
    private PortfolioFinder portfolioFinder;
    
    @Mock
    private HoldingService holdingService;
    
    @Mock
    private BalanceService balanceService;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private User testUser;
    private Portfolio testPortfolio;
    private Instrument testInstrument;
    private Transaction testTransaction;
    
    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createUser();
        testUser.setId(1L);
        
        testPortfolio = TestDataBuilder.createPortfolio(testUser);
        testPortfolio.setId(1L);
        
        testInstrument = TestDataBuilder.createInstrument("AAPL", "Apple Inc.", new BigDecimal("150.00"));
        testInstrument.setId(1L);
        
        testTransaction = TestDataBuilder.createTransaction(
                testUser, testInstrument, "BUY", 
                new BigDecimal("10"), new BigDecimal("150.00")
        );
        testTransaction.setId(1L);
    }
    
    @Test
    @DisplayName("거래 저장 성공 - 매수")
    void save_Buy_Success() {
        // given
        TransactionInput input = TransactionInput.builder()
                .portfolioName("Test Portfolio")
                .ticker("AAPL")
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("10"))
                .amount(new BigDecimal("1500.00"))
                .transactionDate(LocalDate.now())
                .build();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(portfolioFinder.get("Test Portfolio")).thenReturn(testPortfolio);
            when(instrumentFinder.get("AAPL")).thenReturn(testInstrument);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
            
            // when
            transactionService.save(input);
            
            // then
            verify(transactionRepository, times(1)).save(any(Transaction.class));
            verify(holdingService, times(1)).updateHolding(any(), any(), any(), any());
            verify(balanceService, times(1)).create(any());
        }
    }
    
    @Test
    @DisplayName("거래 저장 성공 - 매도")
    void save_Sell_Success() {
        // given
        TransactionInput input = TransactionInput.builder()
                .portfolioName("Test Portfolio")
                .ticker("AAPL")
                .transactionType(TransactionType.SELL)
                .quantity(new BigDecimal("5"))
                .amount(new BigDecimal("750.00"))
                .transactionDate(LocalDate.now())
                .build();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(portfolioFinder.get("Test Portfolio")).thenReturn(testPortfolio);
            when(instrumentFinder.get("AAPL")).thenReturn(testInstrument);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
            
            // when
            transactionService.save(input);
            
            // then
            verify(transactionRepository, times(1)).save(any(Transaction.class));
            verify(holdingService, times(1)).updateHolding(any(), any(), any(), any());
            verify(balanceService, times(1)).create(any());
        }
    }
    
    @Test
    @DisplayName("거래 저장 실패 - 포트폴리오 없음")
    void save_PortfolioNotFound() {
        // given
        TransactionInput input = TransactionInput.builder()
                .portfolioName("NonExistent Portfolio")
                .ticker("AAPL")
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("10"))
                .amount(new BigDecimal("1500.00"))
                .transactionDate(LocalDate.now())
                .build();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(portfolioFinder.get("NonExistent Portfolio")).thenThrow(
                    new BizException(null, "포트폴리오를 찾을 수 없습니다")
            );
            
            // when & then
            assertThatThrownBy(() -> transactionService.save(input))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("포트폴리오를 찾을 수 없습니다");
        }
    }
    
    @Test
    @DisplayName("거래 저장 실패 - 상품 없음")
    void save_InstrumentNotFound() {
        // given
        TransactionInput input = TransactionInput.builder()
                .portfolioName("Test Portfolio")
                .ticker("INVALID")
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("10"))
                .amount(new BigDecimal("1500.00"))
                .transactionDate(LocalDate.now())
                .build();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserOrThrow).thenReturn(testUser);
            when(portfolioFinder.get("Test Portfolio")).thenReturn(testPortfolio);
            when(instrumentFinder.get("INVALID")).thenThrow(
                    new BizException(null, "상품을 찾을 수 없습니다")
            );
            
            // when & then
            assertThatThrownBy(() -> transactionService.save(input))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("상품을 찾을 수 없습니다");
        }
    }
}