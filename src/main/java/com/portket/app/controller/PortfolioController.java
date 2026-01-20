package com.portket.app.controller;

import com.portket.app.dto.http.DataResponse;
import com.portket.app.dto.http.ListResponse;
import com.portket.app.dto.PortfolioInput;
import com.portket.app.dto.PortfolioOutput;
import com.portket.app.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<Void> createPortfolio(@RequestBody PortfolioInput req) {
        portfolioService.createPortfolio(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 비중 조절
    @PutMapping
    public ResponseEntity<Void> updateWeight(@RequestBody PortfolioInput req) {
        portfolioService.updateWeight(req);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable String name) {
        portfolioService.deletePortfolio(name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<ListResponse<PortfolioOutput>> list() {
        return ResponseEntity.ok(portfolioService.listPortfolio(""));
    }

    @GetMapping("/{name}")
    public ResponseEntity<DataResponse<PortfolioOutput>> get(@PathVariable String name) {
        return ResponseEntity.ok(portfolioService.get(name));
    }
}
