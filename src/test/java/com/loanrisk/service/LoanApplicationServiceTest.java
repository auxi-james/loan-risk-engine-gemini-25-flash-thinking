package com.loanrisk.service;

import com.loanrisk.dto.ApplyLoanRequest;
import com.loanrisk.dto.ApplyLoanResponse;
import com.loanrisk.dto.GetLoanResponse;
import com.loanrisk.entity.Customer;
import com.loanrisk.entity.LoanApplication;
import com.loanrisk.entity.ScoringRule;
import com.loanrisk.exception.LoanApplicationNotFoundException;
import com.loanrisk.repository.CustomerRepository;
import com.loanrisk.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ScoringRuleService scoringRuleService;

    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setDateOfBirth(LocalDate.now().minusYears(30)); // Age 30
        // Add other customer fields as needed for rule evaluation
    }

    @Test
    void applyForLoan_Approved() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest();
        request.setCustomerId(testCustomer.getId());
        request.setLoanAmount(new BigDecimal("10000.00"));
        request.setLoanTermMonths(36);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(UUID.randomUUID());
        loanApplication.setCustomer(testCustomer);
        loanApplication.setCreatedAt(LocalDateTime.now());
        loanApplication.setRiskScore(0.0);
        loanApplication.setRiskLevel("Low");
        loanApplication.setDecision("Approved");
        loanApplication.setExplanation("");

        ScoringRule rule1 = new ScoringRule();
        rule1.setId(1L);
        rule1.setName("Age Rule");
        rule1.setField("customer.age");
        rule1.setOperator(">");
        rule1.setRuleValue("60");
        rule1.setRiskPoints(20);
        rule1.setEnabled(true);

        List<ScoringRule> activeRules = Arrays.asList(rule1);

        when(customerRepository.findById(request.getCustomerId())).thenReturn(Optional.of(testCustomer));
        when(scoringRuleService.getActiveScoringRules()).thenReturn(activeRules);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        // Act
        ApplyLoanResponse response = loanApplicationService.applyForLoan(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getLoanId());
        assertEquals(0, response.getRiskScore()); // Age 30 is not > 60
        assertEquals("Low", response.getRiskLevel());
        assertEquals("Approved", response.getDecision());
        assertEquals("", response.getExplanation());

        verify(customerRepository, times(1)).findById(request.getCustomerId());
        verify(scoringRuleService, times(1)).getActiveScoringRules();
        verify(loanApplicationRepository, times(1)).save(any(LoanApplication.class));
    }

    @Test
    void applyForLoan_ManualReview() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest();
        request.setCustomerId(testCustomer.getId());
        request.setLoanAmount(new BigDecimal("20000.00"));
        request.setLoanTermMonths(60);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(UUID.randomUUID());
        loanApplication.setCustomer(testCustomer);
        loanApplication.setCreatedAt(LocalDateTime.now());
        loanApplication.setRiskScore(30.0);
        loanApplication.setRiskLevel("Medium");
        loanApplication.setDecision("Manual Review");
        loanApplication.setExplanation("Age Rule (+30 points)");

        ScoringRule rule1 = new ScoringRule();
        rule1.setId(1L);
        rule1.setName("Age Rule");
        rule1.setField("customer.age");
        rule1.setOperator(">");
        rule1.setRuleValue("60");
        rule1.setRiskPoints(30);
        rule1.setEnabled(true);

        ScoringRule rule2 = new ScoringRule();
        rule2.setId(2L);
        rule2.setName("Another Rule");
        rule2.setField("some.other.field"); // This rule won't be triggered with current logic
        rule2.setOperator("==");
        rule2.setRuleValue("some_value");
        rule2.setRiskPoints(40);
        rule2.setEnabled(true);

        List<ScoringRule> activeRules = Arrays.asList(rule1, rule2);

        when(customerRepository.findById(request.getCustomerId())).thenReturn(Optional.of(testCustomer));
        when(scoringRuleService.getActiveScoringRules()).thenReturn(activeRules);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        // Act
        ApplyLoanResponse response = loanApplicationService.applyForLoan(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getLoanId());
        assertEquals(30, response.getRiskScore()); // Only age rule triggered
        assertEquals("Medium", response.getRiskLevel());
        assertEquals("Manual Review", response.getDecision());
        assertEquals("Age Rule (+30 points)", response.getExplanation());

        verify(customerRepository, times(1)).findById(request.getCustomerId());
        verify(scoringRuleService, times(1)).getActiveScoringRules();
        verify(loanApplicationRepository, times(1)).save(any(LoanApplication.class));
    }

    @Test
    void applyForLoan_Rejected() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest();
        request.setCustomerId(testCustomer.getId());
        request.setLoanAmount(new BigDecimal("30000.00"));
        request.setLoanTermMonths(84);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(UUID.randomUUID());
        loanApplication.setCustomer(testCustomer);
        loanApplication.setCreatedAt(LocalDateTime.now());
        loanApplication.setRiskScore(60.0);
        loanApplication.setRiskLevel("High");
        loanApplication.setDecision("Rejected");
        loanApplication.setExplanation("Age Rule (+60 points)");

        ScoringRule rule1 = new ScoringRule();
        rule1.setId(1L);
        rule1.setName("Age Rule");
        rule1.setField("customer.age");
        rule1.setOperator(">");
        rule1.setRuleValue("60");
        rule1.setRiskPoints(60);
        rule1.setEnabled(true);

        ScoringRule rule2 = new ScoringRule();
        rule2.setId(2L);
        rule2.setName("High Risk Location");
        rule2.setField("customer.address");
        rule2.setOperator("==");
        rule2.setRuleValue("High Risk City"); // This rule won't be triggered with current logic
        rule2.setRiskPoints(50);
        rule2.setEnabled(true);

        List<ScoringRule> activeRules = Arrays.asList(rule1, rule2);

        when(customerRepository.findById(request.getCustomerId())).thenReturn(Optional.of(testCustomer));
        when(scoringRuleService.getActiveScoringRules()).thenReturn(activeRules);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        // Act
        ApplyLoanResponse response = loanApplicationService.applyForLoan(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getLoanId());
        assertEquals(60, response.getRiskScore()); // Only age rule triggered
        assertEquals("High", response.getRiskLevel());
        assertEquals("Rejected", response.getDecision());
        assertEquals("Age Rule (+60 points)", response.getExplanation());

        verify(customerRepository, times(1)).findById(request.getCustomerId());
        verify(scoringRuleService, times(1)).getActiveScoringRules();
        verify(loanApplicationRepository, times(1)).save(any(LoanApplication.class));
    }

    @Test
    void applyForLoan_CustomerNotFound() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest();
        request.setCustomerId(99L); // Non-existing customer ID
        request.setLoanAmount(new BigDecimal("10000.00"));
        request.setLoanTermMonths(36);

        when(customerRepository.findById(request.getCustomerId())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanApplicationService.applyForLoan(request);
        });
        assertEquals("Customer not found with ID: " + request.getCustomerId(), exception.getMessage());

        verify(customerRepository, times(1)).findById(request.getCustomerId());
        verify(scoringRuleService, times(0)).getActiveScoringRules();
        verify(loanApplicationRepository, times(0)).save(any(LoanApplication.class));
    }

    @Test
    void getLoanApplicationById_ExistingLoan() {
        // Arrange
        UUID loanId = UUID.randomUUID();
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(loanId);
        loanApplication.setCustomer(testCustomer);
        loanApplication.setRiskScore(15.0);
        loanApplication.setRiskLevel("Low");
        loanApplication.setDecision("Approved");
        loanApplication.setExplanation("Low risk");
        loanApplication.setCreatedAt(LocalDateTime.now());

        when(loanApplicationRepository.findById(loanId)).thenReturn(Optional.of(loanApplication));

        // Act
        GetLoanResponse response = loanApplicationService.getLoanApplicationById(loanId);

        // Assert
        assertNotNull(response);
        assertEquals(loanId, response.getLoanId());
        assertEquals(testCustomer.getId(), response.getCustomerId());
        // Assuming loanAmount is mapped from riskScore for now
        assertEquals(BigDecimal.valueOf(loanApplication.getRiskScore()), response.getLoanAmount());
        assertNull(response.getLoanTermMonths()); // loanTermMonths not in entity yet
        assertEquals(loanApplication.getRiskScore().intValue(), response.getRiskScore());
        assertEquals(loanApplication.getRiskLevel(), response.getRiskLevel());
        assertEquals(loanApplication.getDecision(), response.getDecision());
        assertEquals(loanApplication.getExplanation(), response.getExplanation());
        assertEquals(loanApplication.getCreatedAt(), response.getCreatedAt());

        verify(loanApplicationRepository, times(1)).findById(loanId);
    }

    @Test
    void getLoanApplicationById_NonExistingLoan_ThrowsException() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(loanApplicationRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LoanApplicationNotFoundException.class, () -> {
            loanApplicationService.getLoanApplicationById(nonExistingId);
        });

        verify(loanApplicationRepository, times(1)).findById(nonExistingId);
    }

    // TODO: Add tests for computed fields (loanRatio, existingDebtRatio) once implemented
    // TODO: Add tests for different operators and field types
    // TODO: Add tests for edge cases in rule evaluation
}