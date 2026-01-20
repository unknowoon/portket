package com.portket.app.dto;

import com.portket.app.constant.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceSaveInput {

    // 거래구분
    private TransactionType transactionType;
    // 종목코드
    private String ticker;
    // 수량
    private BigDecimal quantity;
    // 평단
    private BigDecimal averagePrice;
}
