package com.portket.app.dto;

import com.portket.app.dto.http.DataResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingListInqOutput {

    private String portfolioName;
    private BigDecimal totalAmountKrw;
    private BigDecimal totalAmountUsd;
    private BigDecimal usdEvaluatedProfit;
    private BigDecimal profitRatio;

    List<HoldingListInquirySubOutput> data;

    public DataResponse<HoldingListInqOutput> toResponse() {
        return DataResponse.<HoldingListInqOutput>builder()
                .data(this)
                .inqYn(true)
                .build();
    }
}
