package com.portket.app.controller;

import com.portket.app.dto.TransactionListInquiryOutput;
import com.portket.app.service.TransactionService;
import com.portket.security.jwt.JwtAuthenticationFilter;
import com.portket.security.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import({JwtAuthenticationFilter.class, JwtUtil.class})
@DisplayName("TransactionController - recent 엔드포인트 테스트")
class TransactionRecentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    @DisplayName("최근 거래 조회 성공 - 기본 limit(5)")
    void recentTransactions_DefaultLimit() throws Exception {
        when(transactionService.recent(5)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/transactions/recent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(transactionService).recent(5);
    }

    @Test
    @WithMockUser
    @DisplayName("최근 거래 조회 성공 - custom limit")
    void recentTransactions_CustomLimit() throws Exception {
        when(transactionService.recent(10)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/transactions/recent").param("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(transactionService).recent(10);
    }

    @Test
    @WithMockUser
    @DisplayName("최근 거래 조회 - totalElements가 실제 데이터 수를 반영")
    void recentTransactions_TotalElementsMatchesDataSize() throws Exception {
        List<TransactionListInquiryOutput> mockData = List.of(
                TransactionListInquiryOutput.builder()
                        .ticker("AAPL").transactionType("BUY")
                        .quantity(new BigDecimal("10")).price(new BigDecimal("150"))
                        .amount(new BigDecimal("1500")).transactionDate(LocalDate.now())
                        .build(),
                TransactionListInquiryOutput.builder()
                        .ticker("GOOGL").transactionType("SELL")
                        .quantity(new BigDecimal("5")).price(new BigDecimal("2800"))
                        .amount(new BigDecimal("14000")).transactionDate(LocalDate.now())
                        .build()
        );
        when(transactionService.recent(5)).thenReturn(mockData);

        mockMvc.perform(get("/api/transactions/recent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @WithMockUser
    @DisplayName("최근 거래 조회 - limit 최솟값 경계(1)")
    void recentTransactions_MinBoundary() throws Exception {
        when(transactionService.recent(1)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/transactions/recent").param("limit", "1"))
                .andExpect(status().isOk());

        verify(transactionService).recent(1);
    }

    @Test
    @WithMockUser
    @DisplayName("최근 거래 조회 - limit 최댓값 경계(50)")
    void recentTransactions_MaxBoundary() throws Exception {
        when(transactionService.recent(50)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/transactions/recent").param("limit", "50"))
                .andExpect(status().isOk());

        verify(transactionService).recent(50);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-100"})
    @WithMockUser
    @DisplayName("최근 거래 조회 실패 - limit가 1 미만이면 에러")
    void recentTransactions_BelowMinLimit(String limit) throws Exception {
        mockMvc.perform(get("/api/transactions/recent").param("limit", limit))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(transactionService, never()).recent(anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"51", "100", "999999"})
    @WithMockUser
    @DisplayName("최근 거래 조회 실패 - limit가 50 초과면 에러")
    void recentTransactions_AboveMaxLimit(String limit) throws Exception {
        mockMvc.perform(get("/api/transactions/recent").param("limit", limit))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(transactionService, never()).recent(anyInt());
    }

    @Test
    @DisplayName("최근 거래 조회 실패 - 인증 없음")
    void recentTransactions_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/transactions/recent"))
                .andExpect(status().isUnauthorized());

        verify(transactionService, never()).recent(anyInt());
    }
}
