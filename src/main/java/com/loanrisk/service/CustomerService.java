package com.loanrisk.service;

import com.loanrisk.entity.Customer;
import com.loanrisk.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {
        logger.info("Creating customer with email: {}", customer.getEmail());
        try {
            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Customer created successfully with ID: {}", savedCustomer.getId());
            return savedCustomer;
        } catch (DataIntegrityViolationException e) {
            logger.error("Error creating customer due to data integrity violation: {}", e.getMessage());
            // Depending on requirements, you might throw a custom exception here
            throw new RuntimeException("Email address already exists.", e);
        } catch (Exception e) {
            logger.error("Error creating customer: {}", e.getMessage(), e);
            throw new RuntimeException("An error occurred while creating the customer.", e);
        }
    }

    public Optional<Customer> getCustomerById(Long id) {
        logger.info("Fetching customer with ID: {}", id);
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            logger.info("Customer found with ID: {}", id);
        } else {
            logger.warn("Customer not found with ID: {}", id);
        }
        return customer;
    }
}