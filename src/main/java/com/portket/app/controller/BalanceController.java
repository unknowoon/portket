package com.portket.app.controller;

import com.portket.app.dto.BalanceListInquiryInput;
import com.portket.app.dto.BalanceListInquiryOutput;
import com.portket.app.dto.BalanceSaveInput;
import com.portket.app.dto.http.DataResponse;
import com.portket.app.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/balances")
@RequiredArgsConstructor
public class BalanceController {
    private final BalanceService balanceService;

    @GetMapping
    public ResponseEntity<DataResponse<BalanceListInquiryOutput>> list(@ModelAttribute BalanceListInquiryInput input) {
        return ResponseEntity.ok(balanceService.list(input).toResponse());
    }

    @PostMapping
    public ResponseEntity<Void> createBalance(@RequestBody BalanceSaveInput req) {
        balanceService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
