# Rewards Program API

A Spring Boot RESTful API that calculates customer reward points 
based on purchase transactions over a rolling 3-month period.

## Reward Calculation Rules

- 2 points for every dollar spent over $100 in a transaction
- 1 point for every dollar spent between $50 and $100 in a transaction
- Example: $120 purchase = 2×$20 + 1×$50 = 90 points

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Data JPA
- H2 In-Memory Database
- JUnit 5 + Mockito

## Project Structure
src/
├── main/java/com/rewards/
│   ├── RewardsProgramApplication.java   # App entry point
│   ├── DataInitializer.java             # Sample data loader
│   ├── controller/RewardsController.java
│   ├── service/RewardsService.java
|   ├── service/RewardsServiceImpl.java
│   ├── model/Transaction.java
│   ├── model/CustomerRewards.java
│   └── repository/TransactionRepo.java
└── test/java/com/rewards/
├── RewardsServiceTest.java          # Unit tests
└── RewardsControllerTest.java       # Integration tests

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/rewards/all | Get rewards for all customers |
| GET | /api/rewards/customer/{id} | Get rewards for a specific customer |
| POST | /api/rewards/transaction | Add a new transaction |

## Sample Response
---json
{
  "customerId": "C001",
  "customerName": "Sushil",
  "monthlyPoints": {
    "FEBRUARY-2025": 115,
    "MARCH-2025": 250
  },
  "totalPoints": 365
}
---

## H2 Console
Access the DB at: `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:rewardsdb`