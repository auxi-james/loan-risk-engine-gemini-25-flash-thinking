# Loan Risk Scoring Engine

This project is a Spring Boot application that implements a basic loan risk scoring engine. It provides RESTful API endpoints for managing customers, loan applications, and scoring rules, and calculates a risk score for loan applications based on predefined rules.

## Features

*   **Project Setup:** Spring Boot application with JPA for data persistence.
*   **Database:** Configured to use H2 in-memory database by default, with support for PostgreSQL. Uses HikariCP for connection pooling.
*   **Entities:**
    *   `Customer`: Represents a customer.
    *   `LoanApplication`: Represents a loan application with a relationship to the `Customer` entity.
    *   `ScoringRule`: Defines rules used for risk scoring.
*   **Repositories:** JPA repositories for `Customer`, `LoanApplication`, and `ScoringRule`.
*   **Services & API Endpoints:**
    *   `CustomerService`: Handles customer-related logic.
        *   `POST /customers`: Create a new customer.
        *   `GET /customers/:id`: Get customer details by ID.
    *   `ScoringRuleService`: Handles scoring rule-related logic.
        *   `GET /rules`: Get all scoring rules.
    *   `LoanApplicationService`: Handles loan application logic, including the core scoring logic.
        *   `POST /loan/apply`: Submit a new loan application.
        *   `GET /loan/:id`: Get loan application details by ID.
*   **Scoring Logic:** Evaluates loan applications against defined `ScoringRule`s to calculate a risk score and determine a decision (e.g., Approved, Rejected).
*   **Initial Data:** Populates initial scoring rule data into the database on application startup using `data.sql`.
*   **Refinements:** Includes input validation, exception handling (e.g., `CustomerNotFoundException`, `LoanApplicationNotFoundException`), and basic logging.

## Setup and Running

### Prerequisites

*   Java 17 or higher
*   Maven

### Building

To build the application, navigate to the project root directory and run:

```bash
./mvnw clean install
```

### Running

To run the application with the default H2 in-memory database:

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.

To configure a different database (e.g., PostgreSQL), update the `src/main/resources/application.properties` file with your database connection details.

## Testing

To run the unit and integration tests:

```bash
./mvnw test