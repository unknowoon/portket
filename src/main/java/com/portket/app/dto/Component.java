package com.portket.app.dto;

import com.portket.app.constant.ComponentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Component {

    private ComponentType componentType;
    private String name; // 자산은 티커
    private BigDecimal weight;
}
