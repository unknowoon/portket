package com.portket.app.dto.record;

import com.portket.app.domain.Balance;

import java.math.BigDecimal;

public record EvaluatedBalance(
        Balance balance,
        BigDecimal weight,
        BigDecimal totalAmount,
        BigDecimal evaluatedAmountUsd,
        BigDecimal evaluatedAmountKrw,
        BigDecimal profitRatio,
        BigDecimal profitAmount
) {
}
