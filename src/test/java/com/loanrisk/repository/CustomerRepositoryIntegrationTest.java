package com.loanrisk.repository;

import com.loanrisk.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY) // Use an embedded in-memory database for tests
public class CustomerRepositoryIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testSaveCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setDateOfBirth(LocalDate.of(1990, 5, 15));
        customer.setAddress("123 Main St");
        customer.setEmail("john.doe@example.com");

        Customer savedCustomer = customerRepository.save(customer);

        assertThat(savedCustomer).isNotNull();
        assertThat(savedCustomer.getId()).isNotNull();
        assertThat(savedCustomer.getFirstName()).isEqualTo("John");
        assertThat(savedCustomer.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void testFindCustomerById() {
        Customer customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setDateOfBirth(LocalDate.of(1985, 10, 20));
        customer.setAddress("456 Oak Ave");
        customer.setEmail("jane.doe@example.com");

        Customer savedCustomer = customerRepository.save(customer);

        Optional<Customer> foundCustomer = customerRepository.findById(savedCustomer.getId());

        assertThat(foundCustomer).isPresent();
        assertThat(foundCustomer.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void testUpdateCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Peter");
        customer.setLastName("Jones");
        customer.setDateOfBirth(LocalDate.of(1992, 3, 10));
        customer.setAddress("789 Pine Ln");
        customer.setEmail("peter.jones@example.com");

        Customer savedCustomer = customerRepository.save(customer);

        savedCustomer.setAddress("321 Maple Dr");
        Customer updatedCustomer = customerRepository.save(savedCustomer);

        assertThat(updatedCustomer.getAddress()).isEqualTo("321 Maple Dr");
    }

    @Test
    void testDeleteCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Mary");
        customer.setLastName("Smith");
        customer.setDateOfBirth(LocalDate.of(1995, 7, 25));
        customer.setAddress("987 Cedar Rd");
        customer.setEmail("mary.smith@example.com");

        Customer savedCustomer = customerRepository.save(customer);
        Long customerId = savedCustomer.getId();

        customerRepository.deleteById(customerId);

        Optional<Customer> deletedCustomer = customerRepository.findById(customerId);
        assertThat(deletedCustomer).isNotPresent();
    }
}