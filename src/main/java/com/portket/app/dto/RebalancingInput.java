package com.portket.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebalancingInput {
    @NotNull(message = "포트폴리오 ID는 필수입니다")
    private Long portfolioId;
    
    @NotNull(message = "총 투자 금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "총 투자 금액은 0보다 커야 합니다")
    private BigDecimal totalInvestmentAmount; // 총 투자 금액
    
    private boolean simulate; // true면 시뮬레이션만, false면 실제 실행
}