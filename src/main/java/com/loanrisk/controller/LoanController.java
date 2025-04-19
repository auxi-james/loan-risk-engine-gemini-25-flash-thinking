package com.loanrisk.controller;

import com.loanrisk.dto.ApplyLoanRequest;
import com.loanrisk.dto.ApplyLoanResponse;
import com.loanrisk.dto.GetLoanResponse;
import com.loanrisk.service.LoanApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/loan")
public class LoanController {

    private final LoanApplicationService loanApplicationService;

    @Autowired
    public LoanController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ApplyLoanResponse> applyLoan(@Valid @RequestBody ApplyLoanRequest request) {
        ApplyLoanResponse response = loanApplicationService.applyForLoan(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetLoanResponse> getLoan(@PathVariable UUID id) {
        GetLoanResponse response = loanApplicationService.getLoanApplicationById(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}