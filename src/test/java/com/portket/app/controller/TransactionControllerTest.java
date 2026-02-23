package com.portket.app.controller;

import com.portket.app.constant.TransactionType;
import com.portket.app.dto.TransactionInput;
import com.portket.app.dto.TransactionListInquiryInput;
import com.portket.app.dto.TransactionListInquiryOutput;
import com.portket.app.dto.http.PaginatedResponse;
import com.portket.app.service.TransactionService;
import com.portket.security.jwt.JwtAuthenticationFilter;
import com.portket.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@WebMvcTest(TransactionController.class)
@Import({JwtAuthenticationFilter.class, JwtUtil.class})
@DisplayName("TransactionController 테스트")
class TransactionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TransactionService transactionService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    @Test
    @WithMockUser
    @DisplayName("거래 생성 성공")
    void createTransaction_Success() throws Exception {
        // given
        TransactionInput input = TransactionInput.builder()
                .portfolioName("Test Portfolio")
                .ticker("AAPL")
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("150.00"))
                .amount(new BigDecimal("1500.00"))
                .transactionDate(LocalDate.now())
                .build();
        
        doNothing().when(transactionService).save(any(TransactionInput.class));
        
        // when & then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isCreated());
        
        verify(transactionService, times(1)).save(any(TransactionInput.class));
    }
    
    @Test
    @WithMockUser
    @DisplayName("거래 생성 실패 - 유효성 검증 실패")
    void createTransaction_ValidationFail() throws Exception {
        // given
        TransactionInput input = TransactionInput.builder()
                .portfolioName("Test Portfolio")
                .ticker("AAPL")
                .transactionType(null) // 필수 값 누락
                .quantity(new BigDecimal("-10")) // 음수
                .transactionDate(LocalDate.now().plusDays(1)) // 미래 날짜
                .build();
        
        // when & then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        
        verify(transactionService, never()).save(any());
    }
    
    @Test
    @WithMockUser
    @DisplayName("거래 목록 조회 성공")
    void listTransactions_Success() throws Exception {
        // given
        PaginatedResponse<TransactionListInquiryOutput> response = PaginatedResponse.<TransactionListInquiryOutput>builder()
                .page(1)
                .totalPage(1)
                .data(Collections.emptyList())
                .build();
        
        when(transactionService.list(any(TransactionListInquiryInput.class))).thenReturn(response);
        
        // when & then
        mockMvc.perform(get("/api/transactions")
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPage").value(1))
                .andExpect(jsonPath("$.data").isArray());
        
        verify(transactionService, times(1)).list(any(TransactionListInquiryInput.class));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    @WithMockUser
    @DisplayName("거래 목록 조회 실패 - size가 1 미만이면 400")
    void listTransactions_SizeBelowMin(String size) throws Exception {
        mockMvc.perform(get("/api/transactions")
                .param("page", "1")
                .param("size", size))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).list(any(TransactionListInquiryInput.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"51", "100"})
    @WithMockUser
    @DisplayName("거래 목록 조회 실패 - size가 50 초과면 400")
    void listTransactions_SizeAboveMax(String size) throws Exception {
        mockMvc.perform(get("/api/transactions")
                .param("page", "1")
                .param("size", size))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).list(any(TransactionListInquiryInput.class));
    }

    @Test
    @WithMockUser
    @DisplayName("거래 목록 조회 성공 - totalElements가 응답에 포함됨")
    void listTransactions_TotalElementsReturned() throws Exception {
        PaginatedResponse<TransactionListInquiryOutput> response = PaginatedResponse.<TransactionListInquiryOutput>builder()
                .page(1)
                .size(10)
                .totalPage(1)
                .totalElements(5)
                .data(Collections.emptyList())
                .build();

        when(transactionService.list(any(TransactionListInquiryInput.class))).thenReturn(response);

        mockMvc.perform(get("/api/transactions")
                .param("page", "1")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("거래 생성 실패 - 인증 없음")
    void createTransaction_Unauthorized() throws Exception {
        // given
        TransactionInput input = TransactionInput.builder()
                .portfolioName("Test Portfolio")
                .ticker("AAPL")
                .transactionType(TransactionType.BUY)
                .quantity(new BigDecimal("10"))
                .amount(new BigDecimal("1500.00"))
                .transactionDate(LocalDate.now())
                .build();
        
        // when & then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        
        verify(transactionService, never()).save(any());
    }
}