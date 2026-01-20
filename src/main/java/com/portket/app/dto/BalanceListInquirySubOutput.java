package com.portket.app.dto;

import com.portket.app.constant.Currency;
import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BalanceListInquirySubOutput {

    private String name;                    // 자산 이름
    private String ticker;                  // 자산 티커
    private String country;                 // 거래국
    private String market;                  // 시장
    private Currency currency;              // 통화
    private BigDecimal currentPrice;        // 종가
    private BigDecimal quantity;            // 수량
    private BigDecimal averagePrice;        // 평단
    private BigDecimal weight;              // 비중
    private BigDecimal totalAmount;         // 평가금액
    private BigDecimal evaluatedAmountUsd;  // USD환산평가금액
    private BigDecimal evaluatedAmountKrw;  // KRW환산평가금액
    private BigDecimal profitRatio;          // 수익률
    private BigDecimal profitAmount;        // 수익금액
}
