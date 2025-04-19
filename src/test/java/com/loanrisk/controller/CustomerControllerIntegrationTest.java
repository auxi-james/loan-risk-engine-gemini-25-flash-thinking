package com.loanrisk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loanrisk.dto.CreateCustomerRequest;
import com.loanrisk.entity.Customer;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        loanApplicationRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void createCustomer_ValidData_ReturnsCreatedCustomer() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1990, 5, 15));
        request.setAddress("123 Main St");
        request.setEmail("john.doe@example.com");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.dateOfBirth").value("1990-05-15"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void createCustomer_InvalidData_ReturnsBadRequest() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest();
        // Missing required fields
        request.setFirstName("");
        request.setLastName("");
        request.setDateOfBirth(null);
        request.setAddress("");
        request.setEmail("");

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCustomerById_ExistingCustomer_ReturnsCustomer() throws Exception {
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setDateOfBirth(LocalDate.of(1985, 10, 20));
        customer.setAddress("456 Oak Ave");
        customer.setEmail("jane.doe@example.com");
        Customer savedCustomer = customerRepository.save(customer);

        mockMvc.perform(get("/customers/{id}", savedCustomer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCustomer.getId()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.dateOfBirth").value("1985-10-20"))
                .andExpect(jsonPath("$.address").value("456 Oak Ave"))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));
    }

    @Test
    void getCustomerById_NonExistingCustomer_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/customers/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}