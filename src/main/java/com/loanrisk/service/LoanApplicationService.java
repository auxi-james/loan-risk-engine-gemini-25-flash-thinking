package com.loanrisk.service;

import com.loanrisk.dto.ApplyLoanRequest;
import com.loanrisk.dto.ApplyLoanResponse;
import com.loanrisk.dto.GetLoanResponse;
import com.loanrisk.entity.Customer;
import com.loanrisk.entity.LoanApplication;
import com.loanrisk.entity.ScoringRule;
import com.loanrisk.repository.CustomerRepository;
import com.loanrisk.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final ScoringRuleService scoringRuleService;

    @Autowired
    public LoanApplicationService(LoanApplicationRepository loanApplicationRepository,
                                  CustomerRepository customerRepository,
                                  ScoringRuleService scoringRuleService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
        this.scoringRuleService = scoringRuleService;
    }

    public ApplyLoanResponse applyForLoan(ApplyLoanRequest request) {
        Optional<Customer> optionalCustomer = customerRepository.findById(request.getCustomerId());

        if (optionalCustomer.isEmpty()) {
            throw new RuntimeException("Customer not found with ID: " + request.getCustomerId());
        }

        Customer customer = optionalCustomer.get();

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setCustomer(customer);
        // Assuming loanAmount and loanTermMonths will be added to LoanApplication entity later
        // For now, we will proceed with scoring based on available customer data and rules
        loanApplication.setCreatedAt(LocalDateTime.now());

        // Process scoring rules (existing logic)
        List<ScoringRule> activeRules = scoringRuleService.getActiveScoringRules();
        double totalRiskScore = 0.0;
        List<String> triggeredRulesExplanation = new ArrayList<>();

        // Computed fields (example: age)
        int age = Period.between(customer.getDateOfBirth(), LocalDate.now()).getYears();
        // Add other computed fields as needed (e.g., loanRatio, existingDebtRatio)

        for (ScoringRule rule : activeRules) {
            boolean ruleTriggered = evaluateRule(rule, loanApplication, customer, age); // Pass computed fields

            if (ruleTriggered) {
                totalRiskScore += rule.getRiskPoints();
                triggeredRulesExplanation.add(rule.getName() + " (+" + rule.getRiskPoints() + " points)");
            }
        }

        loanApplication.setRiskScore(totalRiskScore);
        loanApplication.setRiskLevel(determineRiskLevel(totalRiskScore));
        loanApplication.setDecision(determineDecision(totalRiskScore));
        loanApplication.setExplanation(String.join(", ", triggeredRulesExplanation));

        LoanApplication savedLoanApplication = loanApplicationRepository.save(loanApplication);

        ApplyLoanResponse response = new ApplyLoanResponse();
        response.setLoanId(savedLoanApplication.getId());
        response.setRiskScore(savedLoanApplication.getRiskScore().intValue());
        response.setRiskLevel(savedLoanApplication.getRiskLevel());
        response.setDecision(savedLoanApplication.getDecision());
        response.setExplanation(savedLoanApplication.getExplanation());

        return response;
    }

    public GetLoanResponse getLoanApplicationById(UUID id) {
        Optional<LoanApplication> optionalLoanApplication = loanApplicationRepository.findById(id);

        if (optionalLoanApplication.isEmpty()) {
            return null;
        }

        LoanApplication loanApplication = optionalLoanApplication.get();
        GetLoanResponse response = new GetLoanResponse();
        response.setLoanId(loanApplication.getId());
        response.setCustomerId(loanApplication.getCustomer().getId());
        // Assuming loanAmount will be added to LoanApplication entity later. Using riskScore for now.
        response.setLoanAmount(java.math.BigDecimal.valueOf(loanApplication.getRiskScore()));
        // Assuming loanTermMonths will be added to LoanApplication entity later. Setting to null for now.
        response.setLoanTermMonths(null);
        response.setRiskScore(loanApplication.getRiskScore().intValue());
        response.setRiskLevel(loanApplication.getRiskLevel());
        response.setDecision(loanApplication.getDecision());
        response.setExplanation(loanApplication.getExplanation());
        response.setCreatedAt(loanApplication.getCreatedAt());

        return response;
    }

    private boolean evaluateRule(ScoringRule rule, LoanApplication loanApplication, Customer customer, int age) {
        // Basic rule evaluation logic - needs to be expanded based on actual fields and operators
        // This is a simplified example
        String field = rule.getField();
        String operator = rule.getOperator();
        String ruleValue = rule.getRuleValue();

        try {
            switch (field) {
                case "customer.age":
                    int ruleAge = Integer.parseInt(ruleValue);
                    return compare(age, operator, ruleAge);
                // Add cases for other fields (e.g., loanApplication.amount, customer.creditScore, computed fields)
                default:
                    // Handle unknown fields or throw an exception
                    return false;
            }
        } catch (NumberFormatException e) {
            // Handle invalid ruleValue for numeric comparisons
            return false;
        }
    }

    private <T extends Comparable<T>> boolean compare(T value1, String operator, T value2) {
        // Basic comparison logic - needs to handle different data types
        switch (operator) {
            case ">":
                return value1.compareTo(value2) > 0;
            case "<":
                return value1.compareTo(value2) < 0;
            case "==":
                return value1.compareTo(value2) == 0;
            case "!=":
                return value1.compareTo(value2) != 0;
            case ">=":
                return value1.compareTo(value2) >= 0;
            case "<=":
                return value1.compareTo(value2) <= 0;
            default:
                // Handle unknown operators or throw an exception
                return false;
        }
    }

    private String determineRiskLevel(double riskScore) {
        if (riskScore < 30) {
            return "Low";
        } else if (riskScore < 60) {
            return "Medium";
        } else {
            return "High";
        }
    }

    private String determineDecision(double riskScore) {
        if (riskScore < 30) {
            return "Approved";
        } else if (riskScore < 60) {
            return "Manual Review";
        } else {
            return "Rejected";
        }
    }
}