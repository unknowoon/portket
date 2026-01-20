package com.portket.exception;

import com.portket.app.controller.TransactionController;
import com.portket.app.dto.TransactionInput;
import com.portket.app.service.TransactionService;
import com.portket.security.jwt.JwtAuthenticationFilter;
import com.portket.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import({GlobalExceptionHandler.class, JwtAuthenticationFilter.class, JwtUtil.class})
@DisplayName("GlobalExceptionHandler 테스트")
class GlobalExceptionHandlerTest {
    
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
    @DisplayName("BizException 처리")
    void handleBizException() throws Exception {
        // given
        TransactionInput input = TransactionInput.builder()
                .portfolioName("Test Portfolio")
                .ticker("AAPL")
                .build();
        
        doThrow(new BizException(ErrorCode.NOT_FOUND, "포트폴리오를 찾을 수 없습니다"))
                .when(transactionService).save(any());
        
        // when & then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("포트폴리오를 찾을 수 없습니다"))
                .andExpect(jsonPath("$.path").value("/api/transactions"));
    }
    
    @Test
    @WithMockUser
    @DisplayName("Validation 예외 처리")
    void handleValidationException() throws Exception {
        // given - 유효하지 않은 입력
        String invalidJson = """
                {
                    "portfolioName": "Test Portfolio",
                    "ticker": "AAPL",
                    "transactionType": null,
                    "quantity": -10,
                    "transactionDate": "2025-12-31"
                }
                """;
        
        // when & then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("transactionType")))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("quantity")))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("transactionDate")));
    }
    
    @Test
    @WithMockUser
    @DisplayName("일반 예외 처리")
    void handleGeneralException() throws Exception {
        // given
        TransactionInput input = TransactionInput.builder()
                .portfolioName("Test Portfolio")
                .ticker("AAPL")
                .build();
        
        doThrow(new RuntimeException("Unexpected error"))
                .when(transactionService).save(any());
        
        // when & then
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value(
                        "An unexpected error occurred. Please contact support if this issue persists."))
                .andExpect(jsonPath("$.path").value("/api/transactions"));
        
        // 민감한 정보가 노출되지 않는지 확인
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString("Unexpected error"))));
    }
}