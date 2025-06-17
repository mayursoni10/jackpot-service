# Jackpot Service

A Spring Boot microservice that manages jackpot contributions and rewards for a betting system.
The service processes bets, calculates contributions to jackpot pools, and evaluates winning chances.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Testing](#testing)
- [Database Access](#database-access)
- [Examples](#examples)
- [Troubleshooting](#troubleshooting)

## Features

- **Bet Publishing**: REST API to publish bets with Kafka integration (mockable)
- **Flexible Contribution System**:
  - Fixed percentage contributions
  - Variable contributions that decrease as pool grows
- **Configurable Reward System**:
  - Fixed winning chances
  - Variable chances that increase with pool size
- **In-Memory Database**: H2 database for development/testing
- **Extensible Design**: Easy to add new contribution/reward strategies

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

## Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/mayursoni10/jackpot-service.git
cd jackpot-service
```

### 2. Build the Project
```bash
mvn clean install
```

### 3. Run the Application
```bash
# Run with Kafka disabled (recommended for local development)
mvn spring-boot:run -Dspring-boot.run.arguments="--kafka.enabled=false"

# Or simply
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

### 4. Verify the Service is Running
```bash
curl http://localhost:8080/actuator/health
```

## Architecture

### Core Components

1. **Controllers**
   - `JackpotController`: REST endpoints for bet publishing and reward evaluation

2. **Services**
   - `BetService`: Publishes bet events
   - `JackpotContributionService`: Processes contributions using strategy pattern
   - `JackpotRewardService`: Evaluates winning chances
   - `KafkaProducerService`: Handles Kafka publishing (optional)

3. **Strategies**
   - `FixedContributionStrategy`: Fixed percentage contributions
   - `VariableContributionStrategy`: Dynamic contributions based on pool size
   - `FixedRewardStrategy`: Fixed winning probability
   - `VariableRewardStrategy`: Dynamic winning probability

4. **Entities**
   - `Jackpot`: Jackpot configuration and current state
   - `JackpotContribution`: Record of bet contributions
   - `JackpotContribution`: Record of jackpot wins

## API Documentation

### 1. Publish a Bet

**Endpoint**: `POST /api/v1/bets`

**Request Body**:
```json
{
  "betId": "BET-12345",
  "userId": "USER-789",
  "jackpotId": "JACKPOT-001",
  "betAmount": 100.00
}
```

**Response**: `200 OK`
```
Bet published successfully
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{
    "betId": "BET-12345",
    "userId": "USER-789",
    "jackpotId": "JACKPOT-001",
    "betAmount": 100.00
  }'
```

### 2. Evaluate Jackpot Reward

**Endpoint**: `GET /api/v1/bets/{betId}/evaluate`

**Response**:
```json
{
  "won": false,
  "rewardAmount": 0,
  "message": "Better luck next time!"
}
```

**Winning Response**:
```json
{
  "won": true,
  "rewardAmount": 5250.00,
  "message": "Congratulations! You won the jackpot!"
}
```

**Example**:
```bash
curl http://localhost:8080/api/v1/bets/BET-12345/evaluate
```

## Configuration

### Pre-configured Jackpots

The service initializes with two jackpots:

#### JACKPOT-001 (Fixed Configuration)
- Initial Pool: 1,000
- Contribution: Fixed 5% of bet amount
- Reward Chance: Fixed 0.1% (1 in 1000)

#### JACKPOT-002 (Variable Configuration)
- Initial Pool: 5,000
- Contribution: Starts at 10%, decreases by 0.001% per unit of pool growth
- Reward Chance: Starts at 0.1%, increases as pool grows
- 100% win chance at pool size of 50,000

### Application Properties

```yaml
# Kafka Configuration
kafka:
  enabled: false  # Set to true to enable real Kafka
  topic:
    bets: jackpot-bets

# Database Configuration
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=JackpotControllerTest
mvn test -Dtest=JackpotContributionServiceTest
```

### Manual Testing Examples

#### 1. Test Fixed Contribution (5%)
```bash
# Publish a bet of 1000 to JACKPOT-001
curl -X POST http://localhost:8080/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{
    "betId": "TEST-FIXED-001",
    "userId": "USER-001",
    "jackpotId": "JACKPOT-001",
    "betAmount": 1000.00
  }'

# Contribution will be 50 (5% of 1000)
# Check H2 console to verify
```

#### 2. Test Variable Contribution
```bash
# First bet to JACKPOT-002
curl -X POST http://localhost:8080/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{
    "betId": "TEST-VAR-001",
    "userId": "USER-001",
    "jackpotId": "JACKPOT-002",
    "betAmount": 1000.00
  }'

# Contribution ~10% initially

# After many bets when pool is larger
curl -X POST http://localhost:8080/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{
    "betId": "TEST-VAR-100",
    "userId": "USER-001",
    "jackpotId": "JACKPOT-002",
    "betAmount": 1000.00
  }'

# Contribution will be less than 10% due to pool growth
```

#### 3. Test Reward Evaluation
```bash
# Evaluate multiple bets to test probability
for i in {1..10}; do
  # First publish the bet
  curl -X POST http://localhost:8080/api/v1/bets \
    -H "Content-Type: application/json" \
    -d "{
      \"betId\": \"REWARD-TEST-$i\",
      \"userId\": \"USER-001\",
      \"jackpotId\": \"JACKPOT-001\",
      \"betAmount\": 100.00
    }"

  # Then evaluate it
  echo "Evaluating bet REWARD-TEST-$i:"
  curl http://localhost:8080/api/v1/bets/REWARD-TEST-$i/evaluate
  echo -e "\n"
done
```

## Database Access

### H2 Console Access

1. Navigate to: `http://localhost:8080/h2-console`
2. Use these connection settings:
   - **JDBC URL**: `jdbc:h2:mem:testdb`
   - **Username**: `sa`
   - **Password**: (leave empty)
3. Click "Connect"

### Useful Queries

```sql
-- View all jackpots and their current pools
SELECT * FROM JACKPOT;

-- View recent contributions
SELECT * FROM JACKPOT_CONTRIBUTION ORDER BY CREATED_AT DESC;

-- View total contributions per jackpot
SELECT JACKPOT_ID, COUNT(*) as BET_COUNT, SUM(CONTRIBUTION_AMOUNT) as TOTAL_CONTRIBUTIONS
FROM JACKPOT_CONTRIBUTION
GROUP BY JACKPOT_ID;

-- View all rewards (wins)
SELECT * FROM JACKPOT_REWARD ORDER BY CREATED_AT DESC;

-- Check contribution percentage trends for variable jackpot
SELECT
  JACKPOT_ID,
  STAKE_AMOUNT,
  CONTRIBUTION_AMOUNT,
  (CONTRIBUTION_AMOUNT / STAKE_AMOUNT * 100) as CONTRIBUTION_PERCENTAGE,
  CURRENT_JACKPOT_AMOUNT,
  CREATED_AT
FROM JACKPOT_CONTRIBUTION
WHERE JACKPOT_ID = 'JACKPOT-002'
ORDER BY CREATED_AT;
```

## Examples

### Complete Flow Example

```bash
# 1. Publish a bet
curl -X POST http://localhost:8080/api/v1/bets \
  -H "Content-Type: application/json" \
  -d '{
    "betId": "FLOW-TEST-001",
    "userId": "USER-123",
    "jackpotId": "JACKPOT-001",
    "betAmount": 500.00
  }'

# 2. Check contribution in database (will be 25.00 for 5% of 500)
# Access H2 console and run:
# SELECT * FROM JACKPOT_CONTRIBUTION WHERE BET_ID = 'FLOW-TEST-001';

# 3. Check updated jackpot pool
# SELECT * FROM JACKPOT WHERE JACKPOT_ID = 'JACKPOT-001';

# 4. Evaluate for reward
curl http://localhost:8080/api/v1/bets/FLOW-TEST-001/evaluate

# 5. If won, check reward record and pool reset
# SELECT * FROM JACKPOT_REWARD WHERE BET_ID = 'FLOW-TEST-001';
```

### Batch Processing Example

```bash
# Create a script to simulate multiple bets
for i in {1..50}; do
  curl -X POST http://localhost:8080/api/v1/bets \
    -H "Content-Type: application/json" \
    -d "{
      \"betId\": \"BATCH-$i\",
      \"userId\": \"USER-$((i % 10))\",
      \"jackpotId\": \"JACKPOT-002\",
      \"betAmount\": $((RANDOM % 1000 + 100))
    }"
done
```

## Troubleshooting

### Common Issues

#### 1. Kafka Connection Errors
```
ERROR: Node -1 disconnected
```
**Solution**: Run with `--kafka.enabled=false` or start a Kafka broker

#### 2. Port Already in Use
```
ERROR: Port 8080 is already in use
```
**Solution**: Change port with `--server.port=8081` or stop the conflicting service

#### 3. H2 Console Connection Error
```
Database "~/test" not found
```
**Solution**: Use `jdbc:h2:mem:testdb` as JDBC URL, not file-based paths

#### 4. Build Failures
```bash
# Clean and rebuild
mvn clean install -DskipTests

# If tests are failing
mvn clean test
```

### Debug Mode

Run with debug logging:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.sporty.jackpot=DEBUG"
```

## Development Tips

1. **IntelliJ IDEA**: Import as Maven project, it will automatically configure everything
2. **VS Code**: Install Java Extension Pack for best experience
3. **API Testing**: Use Postman collection (import the curl examples)
4. **Hot Reload**: Use Spring Boot DevTools for automatic restart

## Future Enhancements

- Add more contribution strategies (Progressive, Tiered, etc.)
- Implement real Kafka integration
- Add authentication and authorization
- Create admin endpoints for jackpot management
- Add metrics and monitoring
- Implement scheduled jackpot events

## License

This is a demo project for educational purposes.

## Support

For issues or questions, please check the repository issues or create a new one.