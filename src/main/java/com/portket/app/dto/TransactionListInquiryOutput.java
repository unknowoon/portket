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
public class TransactionListInquiryOutput {

    private long id;
    private String instrumentName;
    private String ticker;
    private String country;
    private TransactionType type;
    private BigDecimal quantity;
    private BigDecimal amount;
    private BigDecimal averagePrice;
}
