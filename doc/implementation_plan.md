# Loan Risk Scoring Engine - Implementation and Testing Plan

This document outlines a step-by-step plan for implementing the Loan Risk Scoring Engine microservice, emphasizing incremental development and rigorous testing at each stage.

## Plan Overview

The implementation will proceed in logical layers, starting with the database and data model, moving through the repository and service layers, and finally implementing the API endpoints and the core rule evaluation logic. Each step includes specific instructions for implementation, testing, running tests, fixing issues, and committing changes.

## Implementation Steps

### Step 1: Project Setup and Database Configuration

**Goal:** Configure the project to connect to a relational database and set up the necessary dependencies for JPA/Hibernate.

1.  **Implement:**
    *   Add necessary dependencies to `pom.xml` for Spring Data JPA, the chosen database driver (H2 for testing, PostgreSQL for development/production), and HikariCP connection pooling.
    *   Configure database connection properties in `src/main/resources/application.properties` for both H2 (for testing) and PostgreSQL.
    *   Enable JPA and configure entity scanning.
2.  **Test:**
    *   Write a simple test class to verify database connectivity and that the Spring context loads correctly with the database configuration.
3.  **Run Tests:**
    *   Execute `./mvnw test` in the terminal.
4.  **Fix Issues:**
    *   Address any errors reported by the test execution or build process.
5.  **Commit Changes:**
    *   Commit the changes with a message like "feat: Configure database connection and dependencies".

### Step 2: Customer Entity and Repository

**Goal:** Create the `Customer` entity and a Spring Data JPA repository for managing customer data.

1.  **Implement:**
    *   Create the `Customer` entity class in `src/main/java/com/loanrisk/` based on the requirements, including appropriate JPA annotations (`@Entity`, `@Id`, `@GeneratedValue`, `@Column`, etc.).
    *   Create a `CustomerRepository` interface extending `JpaRepository` in `src/main/java/com/loanrisk/`.
2.  **Test:**
    *   Write an integration test class for `CustomerRepository` using an in-memory database (H2).
    *   Write test methods to:
        *   Save a new customer and verify it's persisted.
        *   Find a customer by ID.
        *   Update a customer.
        *   Delete a customer.
3.  **Run Tests:**
    *   Execute `./mvnw test`.
4.  **Fix Issues:**
    *   Address any test failures or compilation errors.
5.  **Commit Changes:**
    *   Commit the changes with a message like "feat: Implement Customer entity and repository".

### Step 3: Loan Application Entity and Repository

**Goal:** Create the `LoanApplication` entity with its relationship to `Customer` and a repository for loan application data.

1.  **Implement:**
    *   Create the `LoanApplication` entity class in `src/main/java/com/loanrisk/` based on the requirements.
    *   Establish the many-to-one relationship with the `Customer` entity using `@ManyToOne`.
    *   Include fields for `riskScore`, `riskLevel`, `decision`, `explanation`, and `createdAt`.
    *   Create a `LoanApplicationRepository` interface extending `JpaRepository`.
2.  **Test:**
    *   Write an integration test class for `LoanApplicationRepository`.
    *   Write test methods to:
        *   Save a loan application linked to a customer.
        *   Find a loan application by ID.
        *   Verify the relationship with the customer is correctly loaded.
3.  **Run Tests:**
    *   Execute `./mvnw test`.
4.  **Fix Issues:**
    *   Address any test failures or compilation errors.
5.  **Commit Changes:**
    *   Commit the changes with a message like "feat: Implement LoanApplication entity and repository".

### Step 4: Scoring Rule Entity and Repository

**Goal:** Create the `ScoringRule` entity and a repository for managing the dynamic scoring rules.

1.  **Implement:**
    *   Create the `ScoringRule` entity class in `src/main/java/com/loanrisk/` based on the requirements.
    *   Include fields for `name`, `field`, `operator`, `value`, `riskPoints`, `priority`, and `enabled`.
    *   Create a `ScoringRuleRepository` interface extending `JpaRepository`.
2.  **Test:**
    *   Write an integration test class for `ScoringRuleRepository`.
    *   Write test methods to:
        *   Save a scoring rule.
        *   Find all enabled scoring rules, ordered by priority.
        *   Update and delete rules.
3.  **Run Tests:**
    *   Execute `./mvnw test`.
4.  **Fix Issues:**
    *   Address any test failures or compilation errors.
5.  **Commit Changes:**
    *   Commit the changes with a message like "feat: Implement ScoringRule entity and repository".

### Step 5: Customer Service and API Endpoint (`POST /customers`, `GET /customers/:id`)

**Goal:** Implement the service layer logic and REST endpoints for creating and fetching customer profiles.

1.  **Implement:**
    *   Create a `CustomerService` class with methods for creating and retrieving customers, utilizing `CustomerRepository`.
    *   Create a `CustomerController` class using `@RestController` and `@RequestMapping("/customers")`.
    *   Implement the `POST /customers` endpoint to accept customer data and use `CustomerService` to save it.
    *   Implement the `GET /customers/:id` endpoint to fetch customer details using `CustomerService`.
    *   Include necessary request/response DTOs if required.
2.  **Test:**
    *   Write an integration test class for `CustomerController` using `@SpringBootTest` and `MockMvc`.
    *   Write test methods to:
        *   Test the `POST /customers` endpoint with valid and invalid data.
        *   Test the `GET /customers/:id` endpoint for existing and non-existing customers.
3.  **Run Tests:**
    *   Execute `./mvnw test`.
4.  **Fix Issues:**
    *   Address any test failures, API response issues, or validation errors.
5.  **Commit Changes:**
    *   Commit the changes with a message like "feat: Implement Customer service and API endpoints".

### Step 6: Rule Service and API Endpoint (`GET /rules`)

**Goal:** Implement the service layer logic and REST endpoint for retrieving active scoring rules.

1.  **Implement:**
    *   Create a `ScoringRuleService` class with a method to retrieve all enabled scoring rules, utilizing `ScoringRuleRepository`.
    *   Add the `GET /rules` endpoint to a relevant controller (e.g., a new `RuleController` or an existing one if appropriate).
    *   Implement the endpoint to return the list of active rules using `ScoringRuleService`.
2.  **Test:**
    *   Write an integration test class for the rules endpoint.
    *   Write test methods to:
        *   Test the `GET /rules` endpoint to ensure it returns a list of rules.
        *   Verify that only enabled rules are returned and they are ordered by priority.
3.  **Run Tests:**
    *   Execute `./mvnw test`.
4.  **Fix Issues:**
    *   Address any test failures or API response issues.
5.  **Commit Changes:**
    *   Commit the changes with a message like "feat: Implement Rule service and GET /rules endpoint".

### Step 7: Loan Application Service and Core Scoring Logic

**Goal:** Implement the core logic for processing loan applications, including loading rules and calculating the risk score dynamically.

1.  **Implement:**
    *   Create a `LoanApplicationService` class with a method to process a loan application.
    *   Inside this method:
        *   Retrieve the customer associated with the application.
        *   Load all enabled scoring rules using `ScoringRuleService`.
        *   Implement the logic to iterate through the rules and evaluate them against the loan application and customer data.
        *   Implement logic to handle computed fields (e.g., `loanRatio`, `existingDebtRatio`).
        *   Calculate the total `riskScore` by summing up the `riskPoints` of triggered rules.
        *   Determine the `riskLevel` and `decision` based on the calculated score using the provided ranges.
        *   Store the list of triggered rules in the `explanation` field.
        *   Persist the updated `LoanApplication` entity using `LoanApplicationRepository`.
    *   Consider creating helper classes or methods for rule evaluation logic to keep the service clean.
2.  **Test:**
    *   Write comprehensive unit tests for `LoanApplicationService` (mocking repositories and services).
    *   Write test methods to cover various scenarios:
        *   Applications that result in "approve" (low score).
        *   Applications that result in "manual_review" (medium score).
        *   Applications that result in "reject" (high score).
        *   Tests for specific rules being triggered based on input data.
        *   Tests for computed fields.
        *   Tests for edge cases in rule evaluation.
3.  **Run Tests:**
    *   Execute `./mvnw test`.
4.  **Fix Issues:**
    *   Address any test failures or logic errors in the scoring process.
5.  **Commit Changes:**
    *   Commit the changes with a message like "feat: Implement core loan scoring logic in LoanApplicationService".

### Step 8: Loan Application API Endpoints (`POST /loan/apply`, `GET /loan/:id`)

**Goal:** Implement the REST endpoints for submitting loan applications and fetching their results.

1.  **Implement:**
    *   Create a `LoanController` class using `@RestController` and `@RequestMapping("/loan")`.
    *   Implement the `POST /loan/apply` endpoint to accept loan application data, use `LoanApplicationService` to process it, and return the result (loanId, riskScore, riskLevel, decision, explanation).
    *   Implement the `GET /loan/:id` endpoint to fetch a loan application and its evaluation result using `LoanApplicationService` (add a method to the service if needed).
    *   Include necessary request/response DTOs.
2.  **Test:**
    *   Write integration tests for `LoanController` using `MockMvc`.
    *   Write test methods to:
        *   Test the `POST /loan/apply` endpoint with various inputs to verify correct scoring and response.
        *   Test the `GET /loan/:id` endpoint for existing and non-existing loan applications.
        *   Ensure proper handling of validation errors for input data.
3.  **Run Tests:**
    *   Execute `./mvnw test`.
4.  **Fix Issues:**
    *   Address any test failures, API response issues, or validation errors.
5.  **Commit Changes:**
    *   Commit the changes with a message like "feat: Implement Loan application API endpoints".

### Step 9: Initial Rule Data Population

**Goal:** Populate the `ScoringRule` table with the example rules from the requirements on application startup.

1.  **Implement:**
    *   Choose a mechanism for data population:
        *   Using `data.sql` in `src/main/resources` (Spring Boot automatically executes this).
        *   Creating a Spring `CommandLineRunner` or `ApplicationRunner` bean to insert rules programmatically on startup.
    *   Write SQL insert statements (for `data.sql`) or Java code (for `CommandLineRunner`) to add the example rules from the requirements document into the `scoring_rule` table.
2.  **Test:**
    *   Write an integration test that verifies the `ScoringRule` table contains the expected number of rules after the application context loads.
3.  **Run Tests:**
    *   Execute `./mvnw test`.
4.  **Fix Issues:**
    *   Address any issues with data insertion or test failures.
5.  **Commit Changes:**
    *   Commit the changes with a message like "feat: Populate initial scoring rules".

### Step 10: Refinement and Edge Cases

**Goal:** Review the entire implementation, handle edge cases, and improve code quality.

1.  **Implement:**
    *   Review all code for potential null pointers, invalid inputs, or other edge cases not yet covered.
    *   Add validation annotations to DTOs and entities where appropriate.
    *   Implement global exception handling for REST controllers.
    *   Refactor code for better readability, maintainability, and performance.
    *   Add logging where necessary.
2.  **Test:**
    *   Add new unit and integration tests specifically targeting identified edge cases and error scenarios.
    *   Ensure existing tests still pass after refactoring.
3.  **Run Tests:**
    *   Execute `./mvnw test`.
4.  **Fix Issues:**
    *   Address any new or existing test failures.
5.  **Commit Changes:**
    *   Commit the changes with a message like "refactor: Address edge cases and improve code quality".

## Testing Strategy

*   **Unit Tests:** Focus on testing individual classes and methods in isolation, mocking dependencies. Use JUnit and Mockito.
*   **Integration Tests:** Test the interaction between different components (e.g., repository with database, controller with service). Use `@SpringBootTest` and test databases (H2).
*   **API Tests:** Test the REST endpoints using `MockMvc` or a similar framework to ensure they behave as expected and handle various request/response scenarios correctly.

## Running Tests

All tests can be executed from the project root directory using the Maven wrapper:

```bash
./mvnw test
```

This command will compile the code, run all tests, and report the results. Ensure all tests pass before committing changes for each step.