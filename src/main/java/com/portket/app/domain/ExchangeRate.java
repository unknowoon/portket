package com.portket.app.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "exchange_rates")
public class ExchangeRate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String currencyPair;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Column(precision = 10, scale=4)
    private BigDecimal open;

    @Column(precision = 10, scale=4)
    private BigDecimal high;

    @Column(precision = 10, scale=4)
    private BigDecimal low;

    @Column(precision = 10, scale=4)
    private BigDecimal close;

    @Column(precision = 8)
    private Long volume;

    @Column(precision = 10, scale=4)
    private BigDecimal adjClose;
}
