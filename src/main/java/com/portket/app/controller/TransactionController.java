package com.portket.app.controller;

import com.portket.app.dto.TransactionInput;
import com.portket.app.dto.TransactionListInquiryInput;
import com.portket.app.dto.TransactionListInquiryOutput;
import com.portket.app.dto.http.ListResponse;
import com.portket.app.dto.http.PaginatedResponse;
import com.portket.app.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Void> createTransaction(@Valid @RequestBody TransactionInput input) {
        transactionService.save(input);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<TransactionListInquiryOutput>> listTransactions(@Valid @ModelAttribute TransactionListInquiryInput input) {
        return ResponseEntity.ok(transactionService.list(input));
    }

    /**
     * 최근 거래 내역 조회 (대시보드용)
     */
    @GetMapping("/recent")
    public ResponseEntity<ListResponse<TransactionListInquiryOutput>> recentTransactions(
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limit) {
        List<TransactionListInquiryOutput> recent = transactionService.recent(limit);
        return ResponseEntity.ok(ListResponse.<TransactionListInquiryOutput>builder()
                .data(recent)
                .totalElements(recent.size())
                .build());
    }
}
