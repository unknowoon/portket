package com.portket.app.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "holdings")
public class Holding extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="portfolio_id", nullable=false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="instrument_id", nullable=false)
    private Instrument instrument;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal averagePrice;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    public void changeQuantityAndAveragePrice(BigDecimal quantity, BigDecimal averagePrice) {
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    public void changeWeight(BigDecimal weight) {
        this.weight = weight;
    }

}