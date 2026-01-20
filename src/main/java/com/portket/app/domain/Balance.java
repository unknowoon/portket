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
@Table(name = "balances")
public class Balance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="instrument_id", nullable=false)
    private Instrument instrument;

    @Column(precision = 10, scale=2, nullable=false)
    private BigDecimal quantity;

    @Column(name="average_price", precision=10, scale=2, nullable=false)
    private BigDecimal averagePrice;

    public void changeQuantityAndAveragePrice(BigDecimal quantity, BigDecimal averagePrice) {
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }
}
