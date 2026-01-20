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
public class HoldingInput {

    // 거래구분
    private TransactionType transactionType;
    // 포트폴리오 이름
    private String portfolioName;
    // 종목코드
    private String ticker;
    // 수량
    private BigDecimal quantity;
    // 평단
    private BigDecimal averagePrice;
    // 현재비중 TODO 입력받을게 아니라 내부에서 조회해서 사용하는게 나을거 같음 0209
    private BigDecimal weight;
}
