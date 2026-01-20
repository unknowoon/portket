package com.portket.app.controller;

import com.portket.app.dto.TransactionInput;
import com.portket.app.dto.TransactionListInquiryInput;
import com.portket.app.dto.TransactionListInquiryOutput;
import com.portket.app.dto.http.PaginatedResponse;
import com.portket.app.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
