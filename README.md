# Rewards Program API

A Spring Boot RESTful API that calculates customer reward points
based on purchase transactions over a dynamic rolling 3-month period.

---

## Table of Contents
- [Reward Calculation Rules](#reward-calculation-rules)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [API Endpoints](#api-endpoints)
- [Sample Requests & Responses](#sample-requests-responses)
- [Running Tests](#running-tests)
- [H2 Database Console](#h2-database-console)
- [Pre-loaded Sample Data](#pre-loaded-sample-data)
- [Security](#security)

---

## Reward Calculation Rules

| Purchase Amount   | Points Earned                                                        |
|-------------------|----------------------------------------------------------------------|
| $0 – $50          | 0 points                                                             |
| $50.01 – $100     | 1 point per complete dollar above $50                                |
| Above $100        | 2 points per complete dollar above $100 + 50 points for $50–$100 band |

> **Note:** Only **complete dollars** earn points. Cents are ignored (floor, not round).
> BigDecimal is used for precise money calculations to avoid floating point errors.

### Calculation Examples

| Purchase Amount | Calculation                              | Points |
|-----------------|------------------------------------------|--------|
| $30.00          | Below $50                                | 0      |
| $50.99          | $0.99 above $50 → 0 complete dollars     | 0      |
| $51.00          | $1 above $50 × 1                         | 1      |
| $75.00          | $25 above $50 × 1                        | 25     |
| $100.00         | $50 above $50 × 1                        | 50     |
| $100.50         | $0.50 above $100 → 0 complete dollars + 50 | 50   |
| $120.00         | ($20 × 2) + 50                           | 90     |
| $200.00         | ($100 × 2) + 50                          | 250    |

---

## Tech Stack

| Technology      | Version | Purpose                      |
|-----------------|---------|------------------------------|
| Java            | 17      | Programming language         |
| Spring Boot     | 3.2.5   | Application framework        |
| Spring Data JPA | -       | Database access layer        |
| Spring Security | -       | API authentication           |
| H2 Database     | -       | In-memory database           |
| JUnit 5         | -       | Unit and integration testing |
| Mockito         | -       | Mocking framework for tests  |
| Maven           | 3.8+    | Build and dependency tool    |

---

## Project Structure

```
rewards-program/
├── src/
│   ├── main/java/com/rewards/
│   │   ├── RewardsProgramApplication.java        ← App entry point
│   │   ├── DataInitializer.java                  ← Sample data loader (saveAll)
│   │   ├── config/
│   │   │   └── SecurityConfig.java               ← Spring Security configuration
│   │   ├── controller/
│   │   │   └── RewardsController.java            ← REST API endpoints
│   │   ├── dto/
│   │   │   └── ErrorResponse.java                ← Standardized error response
│   │   ├── service/
│   │   │   ├── RewardsService.java               ← Service interface
│   │   │   └── RewardsServiceImpl.java           ← Business logic implementation
│   │   ├── model/
│   │   │   ├── Transaction.java                  ← JPA entity (with DB indexes)
│   │   │   └── CustomerRewards.java              ← API response model
│   │   └── repository/
│   │       └── TransactionRepo.java        		 ← Data access layer
│   ├── main/resources/
│   │   └── application.properties                ← App configuration
│   └── test/java/com/rewards/
│       ├── RewardsServiceTest.java               ← Unit tests (Mockito)
│       └── RewardsControllerIntegrationTest.java ← Integration tests (MockMvc)
├── .gitignore
├── pom.xml
└── README.md
```

---

## How to Run

### Prerequisites
- Java 17+
- Maven 3.8+
- Git

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/sushil023/rewards-program.git

# 2. Navigate into the project folder
cd rewards-program

# 3. Build the project
mvn clean install

# 4. Run the application
mvn spring-boot:run
```

The application starts at: **http://localhost:8080**

---

## API Endpoints

| Method | Endpoint                             | Description                       | Auth Required |
|--------|--------------------------------------|-----------------------------------|---------------|
| GET    | `/api/rewards/all`                   | Get rewards for all customers     | Yes           |
| GET    | `/api/rewards/customer/{customerId}` | Get rewards for one customer      | Yes           |
| POST   | `/api/rewards/transaction`           | Add a new purchase transaction    | Yes           |

---

## Sample Requests & Responses

### 1. GET All Customer Rewards

**Request:**
```
GET http://localhost:8080/api/rewards/all
Authorization: Basic user:user123
```

**Response: HTTP 200 OK**
```json
[
  {
    "customerId": "C001",
    "customerName": "Sushil",
    "monthlyPoints": {
      "JANUARY-2025": 115,
      "FEBRUARY-2025": 250
    },
    "totalPoints": 365
  },
  {
    "customerId": "C002",
    "customerName": "Pramod",
    "monthlyPoints": {
      "JANUARY-2025": 5,
      "FEBRUARY-2025": 110
    },
    "totalPoints": 115
  },
  {
    "customerId": "C003",
    "customerName": "Khiru",
    "monthlyPoints": {
      "JANUARY-2025": 350,
      "MARCH-2025": 70
    },
    "totalPoints": 420
  }
]
```

**Response: HTTP 204 No Content**
```
(Returned when no transactions exist in the last 3 months)
```

---

### 2. GET Rewards for Specific Customer

**Request:**
```
GET http://localhost:8080/api/rewards/customer/C001
Authorization: Basic user:user123
```

**Response: HTTP 200 OK**
```json
{
  "customerId": "C001",
  "customerName": "Sushil",
  "monthlyPoints": {
    "JANUARY-2025": 115,
    "FEBRUARY-2025": 250
  },
  "totalPoints": 365
}
```

**Response: HTTP 404 Not Found**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No transactions found for customer ID: C999",
  "timestamp": "2025-04-07T10:30:00"
}
```

**Response: HTTP 400 Bad Request**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Customer ID must not be null or blank.",
  "timestamp": "2025-04-07T10:30:00"
}
```

---

### 3. POST Add New Transaction

**Request:**
```
POST http://localhost:8080/api/rewards/transaction
Authorization: Basic admin:admin123
Content-Type: application/json

{
  "customerId": "C004",
  "customerName": "Dev",
  "amount": 150.00,
  "transactionDate": "2025-03-15"
}
```

**Response: HTTP 201 Created**
```json
{
  "id": 11,
  "customerId": "C004",
  "customerName": "Dev",
  "amount": 150.00,
  "transactionDate": "2025-03-15"
}
```

**Response: HTTP 400 Bad Request (negative amount)**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be zero or positive.",
  "timestamp": "2025-04-07T10:30:00"
}
```

**Response: HTTP 400 Bad Request (future date)**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Transaction date cannot be null or in the future.",
  "timestamp": "2025-04-07T10:30:00"
}
```

**Response: HTTP 401 Unauthorized (no credentials)**
```json
{
  "status": 401,
  "error": "Unauthorized"
}
```

---

## Running Tests

```bash
# Run all tests
mvn clean test

# Run only unit tests
mvn test -Dtest=RewardsServiceTest

# Run only integration tests
mvn test -Dtest=RewardsControllerIntegrationTest

# Run with detailed console output
mvn test -Dsurefire.useFile=false
```

### Test Coverage

| Test Class                          | Type        | What It Tests                                  |
|-------------------------------------|-------------|------------------------------------------------|
| RewardsServiceTest                  | Unit        | Point calculations, edge cases, service logic  |
| RewardsControllerIntegrationTest    | Integration | API endpoints, security, error responses       |

### Test Scenarios Covered

- ✅ Point calculation for all spending ranges
- ✅ Fractional amount edge cases ($50.99, $100.50)
- ✅ Negative amount validation
- ✅ Null and blank customer ID validation
- ✅ Future date validation
- ✅ Unknown customer 404 handling
- ✅ Date boundary conditions
- ✅ Multiple customers in one response
- ✅ Unauthenticated request returns 401
- ✅ End-to-end POST then GET flow

---

## H2 Database Console

While the application is running, access the browser-based DB console at:

**URL:** http://localhost:8080/h2-console
**JDBC URL:** `jdbc:h2:mem:rewardsdb`
**Username:** `sa`
**Password:** *(leave blank)*

> H2 console does not require authentication and is for development use only.

---

## Pre-loaded Sample Data

The following transactions are auto-loaded on startup via `DataInitializer.java`:

| Customer ID | Name  | Amount  | Month          |
|-------------|-------|---------|----------------|
| C001        | Sushil | $120.00 | 2 months ago   |
| C001        | Sushil | $75.00  | 2 months ago   |
| C001        | Sushil | $200.00 | 1 month ago    |
| C001        | Sushil | $30.00  | Current month  |
| C002        | Pramod   | $55.00  | 2 months ago   |
| C002        | Pramod   | $130.00 | 1 month ago    |
| C002        | Pramod   | $95.00  | Current month  |
| C003        | Khiru | $250.00 | 2 months ago   |
| C003        | Khiru | $45.00  | 1 month ago    |
| C003        | Khiru | $110.00 | Current month  |

> Dates are dynamically calculated from today — **no months are hardcoded**.

---

## Security

This API is secured using **Spring Security with HTTP Basic Authentication**.

### Default Credentials

| Username | Password  | Role  | Access              |
|----------|-----------|-------|---------------------|
| admin    | admin123  | ADMIN | Full access         |
| user     | user123   | USER  | Read access         |

> ⚠️ These are development credentials only.
> In production, replace with a database-backed UserDetailsService and environment variables.

### How to Call Secured Endpoints

**Using curl:**
```bash
# GET all rewards
curl -u user:user123 \
  http://localhost:8080/api/rewards/all

# GET specific customer rewards
curl -u user:user123 \
  http://localhost:8080/api/rewards/customer/C001

# POST new transaction
curl -u admin:admin123 \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "C005",
    "customerName": "Ram",
    "amount": 120.0,
    "transactionDate": "2025-03-01"
  }' \
  http://localhost:8080/api/rewards/transaction
```

**Using Postman:**
```
1. Open Postman
2. Go to Authorization tab
3. Select Type: Basic Auth
4. Enter Username: admin (or user)
5. Enter Password: admin123 (or user123)
6. Send your request
```

---

## Key Implementation Notes

| # | Note |
|---|------|
| 1 | Months are **never hardcoded** — always calculated from `LocalDate.now()` |
| 2 | `BigDecimal` used for all money calculations to avoid floating point errors |
| 3 | Database indexes added on `customerId` and `transactionDate` for query performance |
| 4 | `saveAll()` used in DataInitializer for a single efficient DB round-trip |
| 5 | Service layer split into Interface (`RewardsService`) and Implementation (`RewardsServiceImpl`) |
| 6 | Consistent `ErrorResponse` DTO returned for all error scenarios |
| 7 | `groupAndCalculateRewards()` is private in Impl — not exposed via interface |
| 8 | All public methods have JavaDoc at class and method level |

---

## Author

*Sushil Kumar Sahu*
GitHub: [https://github.com/sushil023](https://github.com/sushil023)
