package com.portket.app.dto;

import com.portket.app.dto.http.PaginatedRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentInput extends PaginatedRequest {

    private String country;
    private String market;
}
