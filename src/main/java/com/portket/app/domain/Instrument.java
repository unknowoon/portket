package com.portket.app.domain;

import com.portket.app.constant.Currency;
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
@Table(name = "instruments")
public class Instrument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 10, nullable = false)
    private String ticker;

    @Column(length = 50, nullable = false)
    private String country;

    @Column(length = 20, nullable = false)
    private String market;

    @Column(length = 3, nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(precision = 14, scale = 2)
    private BigDecimal currentPrice;
}
