package com.portket.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentListInqOutput {

    private Long id;
    private String name;
    private String ticker;
    private String country;
    private String market;
    private String currency;
    private BigDecimal currentPrice;
}
