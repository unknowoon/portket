package com.portket.app.controller;

import com.portket.app.dto.RebalancingInput;
import com.portket.app.dto.RebalancingOutput;
import com.portket.app.service.RebalancingService;
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
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@WebMvcTest(RebalancingController.class)
@Import({JwtAuthenticationFilter.class, JwtUtil.class})
@DisplayName("RebalancingController 테스트")
class RebalancingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private RebalancingService rebalancingService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    private RebalancingOutput mockOutput;
    
    @BeforeEach
    void setUp() {
        RebalancingOutput.RebalancingItem item1 = RebalancingOutput.RebalancingItem.builder()
                .instrumentId(1L)
                .ticker("AAPL")
                .instrumentName("Apple Inc.")
                .targetWeight(new BigDecimal("60"))
                .currentWeight(new BigDecimal("30"))
                .targetValue(new BigDecimal("6000"))
                .currentValue(new BigDecimal("3000"))
                .currentQuantity(new BigDecimal("20"))
                .currentPrice(new BigDecimal("150"))
                .requiredQuantity(new BigDecimal("20"))
                .requiredValue(new BigDecimal("3000"))
                .action("BUY")
                .build();
        
        mockOutput = RebalancingOutput.builder()
                .portfolioId(1L)
                .portfolioName("Test Portfolio")
                .totalInvestmentAmount(new BigDecimal("10000"))
                .items(Arrays.asList(item1))
                .totalCost(new BigDecimal("3000"))
                .simulated(true)
                .build();
    }
    
    @Test
    @WithMockUser
    @DisplayName("리밸런싱 시뮬레이션 성공")
    void simulateRebalancing_Success() throws Exception {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(1L)
                .totalInvestmentAmount(new BigDecimal("10000"))
                .build();
        
        when(rebalancingService.calculateRebalancing(any(RebalancingInput.class))).thenReturn(mockOutput);
        
        // when & then
        mockMvc.perform(post("/api/rebalancing/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.portfolioId").value(1))
                .andExpect(jsonPath("$.data.portfolioName").value("Test Portfolio"))
                .andExpect(jsonPath("$.data.totalInvestmentAmount").value(10000))
                .andExpect(jsonPath("$.data.simulated").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$.data.items[0].action").value("BUY"));
        
        verify(rebalancingService, times(1)).calculateRebalancing(any(RebalancingInput.class));
    }
    
    @Test
    @WithMockUser
    @DisplayName("리밸런싱 실행 성공")
    void executeRebalancing_Success() throws Exception {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(1L)
                .totalInvestmentAmount(new BigDecimal("10000"))
                .build();
        
        mockOutput.setSimulated(false);
        when(rebalancingService.executeRebalancing(any(RebalancingInput.class))).thenReturn(mockOutput);
        
        // when & then
        mockMvc.perform(post("/api/rebalancing/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.simulated").value(false));
        
        verify(rebalancingService, times(1)).executeRebalancing(any(RebalancingInput.class));
    }
    
    @Test
    @WithMockUser
    @DisplayName("리밸런싱 시뮬레이션 실패 - 유효성 검증 실패")
    void simulateRebalancing_ValidationFail() throws Exception {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(null) // 필수값 누락
                .totalInvestmentAmount(new BigDecimal("-1000")) // 음수
                .build();
        
        // when & then
        mockMvc.perform(post("/api/rebalancing/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        
        verify(rebalancingService, never()).calculateRebalancing(any());
    }
    
    @Test
    @DisplayName("리밸런싱 시뮬레이션 실패 - 인증 없음")
    void simulateRebalancing_Unauthorized() throws Exception {
        // given
        RebalancingInput input = RebalancingInput.builder()
                .portfolioId(1L)
                .totalInvestmentAmount(new BigDecimal("10000"))
                .build();
        
        // when & then
        mockMvc.perform(post("/api/rebalancing/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        
        verify(rebalancingService, never()).calculateRebalancing(any());
    }
}