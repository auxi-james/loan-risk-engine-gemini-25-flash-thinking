package com.loanrisk.service;

import com.loanrisk.dto.ApplyLoanRequest;
import com.loanrisk.dto.ApplyLoanResponse;
import com.loanrisk.dto.GetLoanResponse;
import com.loanrisk.entity.Customer;
import com.loanrisk.entity.LoanApplication;
import com.loanrisk.entity.ScoringRule;
import com.loanrisk.exception.CustomerNotFoundException;
import com.loanrisk.exception.LoanApplicationNotFoundException;
import com.loanrisk.repository.CustomerRepository;
import com.loanrisk.repository.LoanApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationService.class);

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
        logger.info("Received loan application request for customer ID: {}", request.getCustomerId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.getCustomerId()));

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
        logger.info("Loan application saved with ID: {}", savedLoanApplication.getId());

        ApplyLoanResponse response = new ApplyLoanResponse();
        response.setLoanId(savedLoanApplication.getId());
        response.setRiskScore(savedLoanApplication.getRiskScore().intValue());
        response.setRiskLevel(savedLoanApplication.getRiskLevel());
        response.setDecision(savedLoanApplication.getDecision());
        response.setExplanation(savedLoanApplication.getExplanation());

        return response;
    }

    public GetLoanResponse getLoanApplicationById(UUID id) {
        logger.info("Fetching loan application with ID: {}", id);
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new LoanApplicationNotFoundException(id));

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

        logger.info("Successfully fetched loan application with ID: {}", id);
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
                    logger.warn("Unknown rule field: {}", field);
                    return false;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid rule value for numeric comparison: {}", ruleValue, e);
            return false;
        } catch (Exception e) {
            logger.error("Error evaluating rule: {}", rule.getName(), e);
            return false;
        }
    }

    private <T extends Comparable<T>> boolean compare(T value1, String operator, T value2) {
        // Basic comparison logic - needs to handle different data types
        if (value1 == null || value2 == null) {
            logger.warn("Attempted to compare null values: value1={}, value2={}", value1, value2);
            return false; // Cannot compare nulls
        }
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
                logger.warn("Unknown comparison operator: {}", operator);
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