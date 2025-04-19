package com.loanrisk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LoanApplicationNotFoundException extends RuntimeException {

    public LoanApplicationNotFoundException(UUID loanId) {
        super("Loan application not found with ID: " + loanId);
    }
}