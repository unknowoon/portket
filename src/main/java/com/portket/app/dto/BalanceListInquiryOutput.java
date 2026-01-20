package com.portket.app.dto;

import com.portket.app.dto.http.DataResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BalanceListInquiryOutput {

    private BigDecimal totalAmountKrw;
    private BigDecimal totalAmountUsd;
    private BigDecimal usdEvaluatedProfit;
    private BigDecimal profitRatio;
    private List<BalanceListInquirySubOutput> data;

    public DataResponse<BalanceListInquiryOutput> toResponse() {
        return DataResponse.<BalanceListInquiryOutput>builder()
                .data(this)
                .inqYn(true)
                .build();
    }
}
