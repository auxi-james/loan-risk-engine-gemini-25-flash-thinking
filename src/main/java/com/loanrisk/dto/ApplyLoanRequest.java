package com.loanrisk.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ApplyLoanRequest {

    @NotNull
    private Long customerId;

    @NotNull
    private BigDecimal loanAmount;

    @NotNull
    private Integer loanTermMonths;

    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Integer getLoanTermMonths() {
        return loanTermMonths;
    }

    public void setLoanTermMonths(Integer loanTermMonths) {
        this.loanTermMonths = loanTermMonths;
    }
}