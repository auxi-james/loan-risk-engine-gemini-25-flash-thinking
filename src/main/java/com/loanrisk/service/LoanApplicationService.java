package com.loanrisk.service;

import com.loanrisk.entity.Customer;
import com.loanrisk.entity.LoanApplication;
import com.loanrisk.entity.ScoringRule;
import com.loanrisk.repository.CustomerRepository;
import com.loanrisk.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public LoanApplication processLoanApplication(Long loanApplicationId) {
        Optional<LoanApplication> optionalLoanApplication = loanApplicationRepository.findById(loanApplicationId);

        if (optionalLoanApplication.isEmpty()) {
            throw new RuntimeException("Loan Application not found with ID: " + loanApplicationId);
        }

        LoanApplication loanApplication = optionalLoanApplication.get();
        Customer customer = loanApplication.getCustomer();

        if (customer == null) {
            throw new RuntimeException("Customer not found for Loan Application ID: " + loanApplicationId);
        }

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

        return loanApplicationRepository.save(loanApplication);
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