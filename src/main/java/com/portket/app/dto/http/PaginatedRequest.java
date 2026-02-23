package com.portket.app.dto.http;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PaginatedRequest {
    @Min(value = 1, message = "page must be at least 1")
    private int page = 1;

    @Min(value = 1, message = "size must be at least 1")
    @Max(value = 50, message = "size must be at most 50")
    private int size = 10;
}
