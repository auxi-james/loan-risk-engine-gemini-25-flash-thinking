package com.loanrisk.service;

import com.loanrisk.entity.Customer;
import com.loanrisk.entity.LoanApplication;
import com.loanrisk.entity.ScoringRule;
import com.loanrisk.repository.CustomerRepository;
import com.loanrisk.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processLoanApplication_Approved() {
        // Arrange
        Long loanApplicationId = 1L;
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setDateOfBirth(LocalDate.now().minusYears(30)); // Age 30

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(loanApplicationId);
        loanApplication.setCustomer(customer);
        loanApplication.setCreatedAt(LocalDateTime.now());

        ScoringRule rule1 = new ScoringRule();
        rule1.setId(1L);
        rule1.setName("Age Rule");
        rule1.setField("customer.age");
        rule1.setOperator(">");
        rule1.setRuleValue("60");
        rule1.setRiskPoints(20);
        rule1.setEnabled(true);

        List<ScoringRule> activeRules = Arrays.asList(rule1);

        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(loanApplication));
        when(scoringRuleService.getActiveScoringRules()).thenReturn(activeRules);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        // Act
        LoanApplication processedApplication = loanApplicationService.processLoanApplication(loanApplicationId);

        // Assert
        assertNotNull(processedApplication);
        assertEquals(0.0, processedApplication.getRiskScore()); // Age 30 is not > 60
        assertEquals("Low", processedApplication.getRiskLevel());
        assertEquals("Approved", processedApplication.getDecision());
        assertEquals("", processedApplication.getExplanation());

        verify(loanApplicationRepository, times(1)).findById(loanApplicationId);
        verify(scoringRuleService, times(1)).getActiveScoringRules();
        verify(loanApplicationRepository, times(1)).save(loanApplication);
    }

    @Test
    void processLoanApplication_ManualReview() {
        // Arrange
        Long loanApplicationId = 2L;
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setDateOfBirth(LocalDate.now().minusYears(70)); // Age 70

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(loanApplicationId);
        loanApplication.setCustomer(customer);
        loanApplication.setCreatedAt(LocalDateTime.now());

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

        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(loanApplication));
        when(scoringRuleService.getActiveScoringRules()).thenReturn(activeRules);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        // Act
        LoanApplication processedApplication = loanApplicationService.processLoanApplication(loanApplicationId);

        // Assert
        assertNotNull(processedApplication);
        assertEquals(30.0, processedApplication.getRiskScore()); // Only age rule triggered
        assertEquals("Medium", processedApplication.getRiskLevel());
        assertEquals("Manual Review", processedApplication.getDecision());
        assertEquals("Age Rule (+30 points)", processedApplication.getExplanation());

        verify(loanApplicationRepository, times(1)).findById(loanApplicationId);
        verify(scoringRuleService, times(1)).getActiveScoringRules();
        verify(loanApplicationRepository, times(1)).save(loanApplication);
    }

    @Test
    void processLoanApplication_Rejected() {
        // Arrange
        Long loanApplicationId = 3L;
        Customer customer = new Customer();
        customer.setId(3L);
        customer.setDateOfBirth(LocalDate.now().minusYears(75)); // Age 75

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(loanApplicationId);
        loanApplication.setCustomer(customer);
        loanApplication.setCreatedAt(LocalDateTime.now());

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

        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(loanApplication));
        when(scoringRuleService.getActiveScoringRules()).thenReturn(activeRules);
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenReturn(loanApplication);

        // Act
        LoanApplication processedApplication = loanApplicationService.processLoanApplication(loanApplicationId);

        // Assert
        assertNotNull(processedApplication);
        assertEquals(60.0, processedApplication.getRiskScore()); // Only age rule triggered
        assertEquals("High", processedApplication.getRiskLevel());
        assertEquals("Rejected", processedApplication.getDecision());
        assertEquals("Age Rule (+60 points)", processedApplication.getExplanation());

        verify(loanApplicationRepository, times(1)).findById(loanApplicationId);
        verify(scoringRuleService, times(1)).getActiveScoringRules();
        verify(loanApplicationRepository, times(1)).save(loanApplication);
    }

    @Test
    void processLoanApplication_LoanApplicationNotFound() {
        // Arrange
        Long loanApplicationId = 99L;
        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanApplicationService.processLoanApplication(loanApplicationId);
        });
        assertEquals("Loan Application not found with ID: " + loanApplicationId, exception.getMessage());

        verify(loanApplicationRepository, times(1)).findById(loanApplicationId);
        verify(scoringRuleService, times(0)).getActiveScoringRules();
        verify(loanApplicationRepository, times(0)).save(any(LoanApplication.class));
    }

    @Test
    void processLoanApplication_CustomerNotFound() {
        // Arrange
        Long loanApplicationId = 4L;
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setId(loanApplicationId);
        loanApplication.setCustomer(null); // No customer

        when(loanApplicationRepository.findById(loanApplicationId)).thenReturn(Optional.of(loanApplication));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanApplicationService.processLoanApplication(loanApplicationId);
        });
        assertEquals("Customer not found for Loan Application ID: " + loanApplicationId, exception.getMessage());

        verify(loanApplicationRepository, times(1)).findById(loanApplicationId);
        verify(scoringRuleService, times(0)).getActiveScoringRules();
        verify(loanApplicationRepository, times(0)).save(any(LoanApplication.class));
    }

    // TODO: Add tests for computed fields (loanRatio, existingDebtRatio) once implemented
    // TODO: Add tests for different operators and field types
    // TODO: Add tests for edge cases in rule evaluation
}