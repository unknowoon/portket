package com.portket.app.dto;

import com.portket.app.constant.TransactionType;
import lombok.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TransactionInput {

    private String portfolioName;
    private Long portfolioId;
    private String ticker;
    private Long instrumentId;
    
    @NotNull(message = "거래 유형은 필수입니다")
    private TransactionType transactionType;
    
    @NotNull(message = "수량은 필수입니다")
    @DecimalMin(value = "0.01", message = "수량은 0보다 커야 합니다")
    private BigDecimal quantity;
    
    @DecimalMin(value = "0", inclusive = true, message = "가격은 0 이상이어야 합니다")
    private BigDecimal price;
    
    @DecimalMin(value = "0", inclusive = true, message = "금액은 0 이상이어야 합니다")
    private BigDecimal amount;
    
    @NotNull(message = "거래일자는 필수입니다")
    @PastOrPresent(message = "거래일자는 미래일 수 없습니다")
    private LocalDate transactionDate;
    
    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다")
    private String memo;
}
