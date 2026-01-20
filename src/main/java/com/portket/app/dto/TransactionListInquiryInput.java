package com.portket.app.dto;

import com.portket.app.dto.http.PaginatedRequest;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TransactionListInquiryInput extends PaginatedRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String ticker;
}
