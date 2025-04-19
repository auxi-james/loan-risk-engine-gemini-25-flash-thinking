package com.loanrisk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanrisk.dto.ApplyLoanRequest;
import com.loanrisk.entity.Customer;
import com.loanrisk.entity.LoanApplication;
import com.loanrisk.repository.CustomerRepository;
import com.loanrisk.repository.LoanApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        loanApplicationRepository.deleteAll();
        customerRepository.deleteAll();

        testCustomer = new Customer();
        testCustomer.setFirstName("John");
        testCustomer.setLastName("Doe");
        testCustomer.setDateOfBirth(LocalDate.of(1990, 5, 15));
        testCustomer.setAddress("123 Main St");
        testCustomer.setCity("Anytown");
        testCustomer.setCountry("USA");
        testCustomer.setZipCode("12345");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setPhoneNumber("555-1234");
        testCustomer.setCreditScore(750);
        testCustomer.setEmploymentStatus("Full-time");
        testCustomer.setAnnualIncome(new BigDecimal("60000.00"));
        testCustomer.setExistingDebt(new BigDecimal("10000.00"));
        testCustomer.setMaritalStatus("Single");
        testCustomer.setNumberOfDependents(0);
        testCustomer.setCreatedAt(LocalDateTime.now());
        customerRepository.save(testCustomer);
    }

    @Test
    void applyLoan_validRequest_returnsLoanDetails() throws Exception {
        ApplyLoanRequest request = new ApplyLoanRequest();
        request.setCustomerId(testCustomer.getId());
        request.setLoanAmount(new BigDecimal("10000.00"));
        request.setLoanTermMonths(36);

        mockMvc.perform(post("/loan/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").exists())
                .andExpect(jsonPath("$.riskScore").exists())
                .andExpect(jsonPath("$.riskLevel").exists())
                .andExpect(jsonPath("$.decision").exists())
                .andExpect(jsonPath("$.explanation").exists());
    }

    @Test
    void applyLoan_invalidRequest_returnsBadRequest() throws Exception {
        ApplyLoanRequest request = new ApplyLoanRequest();
        // Missing customerId, loanAmount, loanTermMonths

        mockMvc.perform(post("/loan/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLoan_existingLoan_returnsLoanDetails() throws Exception {
        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setCustomer(testCustomer);
        loanApplication.setRiskScore(25.0);
        loanApplication.setRiskLevel("Low");
        loanApplication.setDecision("Approved");
        loanApplication.setExplanation("Low risk score");
        loanApplication.setCreatedAt(LocalDateTime.now());
        LoanApplication savedLoan = loanApplicationRepository.save(loanApplication);

        mockMvc.perform(get("/loan/" + savedLoan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(savedLoan.getId().toString()))
                .andExpect(jsonPath("$.riskScore").value(savedLoan.getRiskScore().intValue()))
                .andExpect(jsonPath("$.riskLevel").value(savedLoan.getRiskLevel()))
                .andExpect(jsonPath("$.decision").value(savedLoan.getDecision()))
                .andExpect(jsonPath("$.explanation").value(savedLoan.getExplanation()));
    }

    @Test
    void getLoan_nonExistingLoan_returnsNotFound() throws Exception {
        UUID nonExistingId = UUID.randomUUID();

        mockMvc.perform(get("/loan/" + nonExistingId))
                .andExpect(status().isNotFound());
    }
}