package com.portket.app.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class InstrumentOutput {

    private Long id;
    private String name;
    private String ticker;
    private String country;
    private String market;
    private String currency;
    private BigDecimal currentPrice;
}
