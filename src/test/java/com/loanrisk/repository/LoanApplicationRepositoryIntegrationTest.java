package com.loanrisk.repository;

import com.loanrisk.entity.Customer;
import com.loanrisk.entity.LoanApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LoanApplicationRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Test
    public void whenSaveLoanApplication_thenPersistAndFindById() {
        // Given
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setAddress("123 Main St");
        customer.setDateOfBirth(new Date());
        entityManager.persist(customer);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setCustomer(customer);
        loanApplication.setRiskScore(750.0);
        loanApplication.setRiskLevel("Low");
        loanApplication.setDecision("Approved");
        loanApplication.setExplanation("Good credit history");
        loanApplication.setCreatedAt(LocalDateTime.now());

        // When
        LoanApplication savedLoanApplication = loanApplicationRepository.save(loanApplication);

        // Then
        assertThat(savedLoanApplication).isNotNull();
        assertThat(savedLoanApplication.getId()).isNotNull();

        Optional<LoanApplication> foundLoanApplication = loanApplicationRepository.findById(savedLoanApplication.getId());
        assertThat(foundLoanApplication).isPresent();
        assertThat(foundLoanApplication.get().getCustomer().getId()).isEqualTo(customer.getId());
        assertThat(foundLoanApplication.get().getRiskScore()).isEqualTo(750.0);
    }

    @Test
    public void whenFindById_thenLoadCustomerRelationship() {
        // Given
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setAddress("456 Oak Ave");
        customer.setDateOfBirth(new Date());
        entityManager.persist(customer);

        LoanApplication loanApplication = new LoanApplication();
        loanApplication.setCustomer(customer);
        loanApplication.setRiskScore(600.0);
        loanApplication.setRiskLevel("Medium");
        loanApplication.setDecision("Pending");
        loanApplication.setExplanation("Needs further review");
        loanApplication.setCreatedAt(LocalDateTime.now());
        entityManager.persist(loanApplication);
        entityManager.flush();

        // When
        Optional<LoanApplication> foundLoanApplication = loanApplicationRepository.findById(loanApplication.getId());

        // Then
        assertThat(foundLoanApplication).isPresent();
        assertThat(foundLoanApplication.get().getCustomer()).isNotNull();
        assertThat(foundLoanApplication.get().getCustomer().getFirstName()).isEqualTo("Jane");
        assertThat(foundLoanApplication.get().getCustomer().getLastName()).isEqualTo("Doe");
    }
}