# E-Commerce Microservices Platform

A modern, scalable e-commerce platform built with Spring Boot microservices architecture, featuring JWT authentication, Redis session management, and comprehensive API documentation.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Design Decisions](#design-decisions)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Development](#development)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## 🎯 Overview

This e-commerce platform is a multi-module Spring Boot application implementing a microservices architecture. It provides a complete solution for managing users, products, and orders with role-based access control and secure authentication.

### Key Features

- **Microservices Architecture**: Independent, scalable services for users, products, and orders
- **JWT Authentication**: Secure token-based authentication with Redis session management
- **Role-Based Access Control (RBAC)**: Three-tier role system (USER, PREMIUM_USER, ADMIN)
- **CQRS Pattern**: Command Query Responsibility Segregation for better separation of concerns
- **RESTful APIs**: Comprehensive REST endpoints with OpenAPI/Swagger documentation
- **Database Migrations**: Flyway for version-controlled database schema management
- **Docker Support**: Complete containerization with Docker Compose
- **Comprehensive Testing**: Unit and integration tests with JUnit 5 and Mockito

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        API Gateway                          │
│  (Port 8080)                                                │
│  - Authentication (JWT / OAuth2)                            │
│  - Authorization (RBAC)                                     │
│  - Rate Limiting                                            │
│  - Routing                                                  │
└──────────────────┬──────────────────────────────────────────┘
                   │
    ┌──────────----┼──────────┐
    │              │          │
┌───▼────────┐ ┌───▼────────┐ ┌───▼────────┐
│ User       │ │ Product    │ │ Order      │
│ Service    │ │ Service    │ │ Service    │
│ :8083      │ │ :8081      │ │ :8082      │
│            │ │            │ │            │
│ Owns DB    │ │ Owns DB    │ │ Owns DB    │
└───┬────────┘ └───┬────────┘ └───┬────────┘
    │              │              │
    │              │              │
┌───▼────────┐ ┌───▼────────┐ ┌───▼────────┐
│ user_db    │ │ product_db │ │ order_db   │
│ PostgreSQL │ │ PostgreSQL │ │ PostgreSQL │
└────────────┘ └────────────┘ └────────────┘
               │
               │
        ┌──────▼──────┐
        │   Redis     │
        │             │
        │ - Cache     │
        │ - RateLimit │
        │ - Idempot.  │
        │ - Sessions* │
        └─────────────┘
               │
               │
        ┌──────▼──────┐
        │ Event Bus   │
        │ (NATS /     │
        │  Kafka)     │
        │             │
        │ - UserEvents│
        │ - ProductEv │
        │ - OrderEv   │
        └─────────────┘
 
```

### Service Responsibilities

#### API Gateway (Port 8080)
- **Purpose**: Single entry point for all client requests
- **Responsibilities**:
  - Request routing to appropriate microservices
  - JWT token validation and authentication
  - Session management with Redis
  - Role-based access control enforcement
  - API documentation (Swagger/OpenAPI)

#### User Service (Port 8083)
- **Purpose**: User management and authentication
- **Responsibilities**:
  - User CRUD operations
  - User role management (USER, PREMIUM_USER, ADMIN)
  - User profile management
  - Database: `user_db`

#### Product Service (Port 8081)
- **Purpose**: Product catalog management
- **Responsibilities**:
  - Product CRUD operations
  - Product inventory management
  - Product search and filtering
  - Database: `product_db`

#### Order Service (Port 8082)
- **Purpose**: Order processing and management
- **Responsibilities**:
  - Order creation and management
  - Order status tracking
  - Discount calculation based on user roles
  - Inventory synchronization with Product Service
  - Database: `order_db`

### Shared Module

The `shared` module contains:
- **CQRS Infrastructure**: CommandBus, QueryBus, and handler interfaces
- **DTOs**: Data Transfer Objects for inter-service communication
- **Commands**: Command objects for write operations
- **Validation**: Custom validators and annotations
- **Exceptions**: Shared exception classes

## 🎨 Design Decisions

### 1. Microservices Architecture

**Decision**: Separate services for users, products, and orders

**Rationale**:
- **Scalability**: Each service can scale independently based on load
- **Technology Flexibility**: Services can evolve independently
- **Fault Isolation**: Failures in one service don't cascade to others
- **Team Autonomy**: Different teams can work on different services

### 2. CQRS Pattern

**Decision**: Command Query Responsibility Segregation

**Rationale**:
- **Separation of Concerns**: Clear distinction between read and write operations
- **Scalability**: Read and write models can be optimized separately
- **Maintainability**: Easier to understand and modify business logic
- **Testability**: Commands and queries can be tested independently

### 3. JWT with Redis Session Management

**Decision**: JWT tokens with Redis-backed session validation

**Rationale**:
- **Stateless Authentication**: JWT tokens enable stateless authentication
- **Session Revocation**: Redis allows immediate session invalidation on logout
- **Performance**: Redis provides fast session lookups
- **Scalability**: Redis can be clustered for high availability
- **Security**: Ability to invalidate compromised tokens immediately

### 4. Database per Service

**Decision**: Each microservice has its own database

**Rationale**:
- **Data Isolation**: Services cannot directly access each other's data
- **Independent Scaling**: Each database can be scaled independently
- **Technology Choice**: Each service can choose the best database technology
- **Deployment Independence**: Services can be deployed without affecting others

### 5. API Gateway Pattern

**Decision**: Single entry point for all client requests

**Rationale**:
- **Centralized Security**: Authentication and authorization in one place
- **Request Routing**: Clients don't need to know service locations
- **API Aggregation**: Can combine data from multiple services
- **Rate Limiting**: Can implement rate limiting at the gateway level

### 6. Role-Based Access Control

**Decision**: Three-tier role system (USER, PREMIUM_USER, ADMIN)

**Rationale**:
- **Flexible Permissions**: Different access levels for different user types
- **Business Logic**: PREMIUM_USER gets discounts, ADMIN manages products
- **Security**: Principle of least privilege
- **Scalability**: Easy to add new roles in the future

### 7. OpenAPI/Swagger Documentation

**Decision**: Comprehensive API documentation with Swagger

**Rationale**:
- **Developer Experience**: Interactive API documentation
- **API Testing**: Built-in testing capabilities
- **Contract Definition**: Clear API contracts for frontend developers
- **Maintainability**: Documentation stays in sync with code

### 8. Chain of Responsibility Pattern for Discounts

**Decision**: Discount calculation using Chain of Responsibility / Pipeline pattern

**Rationale**:
- **Extensibility**: Easy to add new discount rules without modifying existing code
- **Separation of Concerns**: Each discount rule is independent and testable
- **Flexibility**: Rules can be reordered, enabled, or disabled via Spring `@Order` annotation
- **Composability**: Multiple discounts can apply simultaneously (additive)
- **Maintainability**: Clear, single-responsibility classes for each discount type
- **Testability**: Each rule can be unit tested independently

### 9. Idempotency Key for Order Creation

**Decision**: Optional idempotency key support for order creation using Redis caching

**Rationale**:
- **Prevent Duplicate Orders**: Network retries, user double-clicks, or system failures can cause duplicate order submissions. Idempotency keys ensure the same request creates only one order
- **Improved User Experience**: Users can safely retry failed requests without worrying about creating duplicate orders
- **Data Integrity**: Prevents accidental duplicate charges and inventory issues
- **Redis as Cache Store**: 
  - **Performance**: Redis provides sub-millisecond lookups, ensuring minimal latency impact
  - **TTL Support**: Built-in expiration (24 hours) automatically cleans up old keys
  - **Distributed**: Works seamlessly in distributed/microservices environments
  - **Scalability**: Redis can handle high-throughput idempotency checks
- **Optional Implementation**: 
  - **Flexibility**: Clients can choose when to use idempotency (e.g., for critical operations)
  - **Backward Compatibility**: Existing clients without idempotency keys continue to work
  - **Best-Effort**: Idempotency failures don't block order creation (graceful degradation)
- **24-Hour Expiration**: 
  - **Balance**: Long enough to handle retries and network issues, short enough to prevent stale data
  - **Configurable**: Expiration time can be adjusted via `idempotency.key.expiration-hours` property
- **Key Format**: Uses prefix `idempotency:order:` for namespace isolation and easy key management

**Implementation Details**:
- Idempotency key is checked **before** any order processing begins
- If a cached order exists, it's returned immediately without database operations
- If no cached order exists, the order is created normally and then cached
- Cache operations are wrapped in try-catch to ensure order creation succeeds even if Redis is unavailable

## 📦 Prerequisites

### Required Software

- **Java 25** or higher
- **Maven 3.9+**
- **Docker** and **Docker Compose** (for containerized deployment)
- **PostgreSQL 16+** (if running services locally)
- **Redis 7+** (if running services locally)

### Optional Tools

- **Postman** or **cURL** for API testing
- **IntelliJ IDEA** or **vsCode** for development
- **pgAdmin** or **tablePlus** for database management

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended)

The easiest way to run the entire platform:

```bash
# Clone the repository
git clone <repository-url>
cd ecomm

# Start all services with Docker Compose
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f api-gateway
```

Services will be available at:
- **API Gateway**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Product Service**: http://localhost:8081
- **Order Service**: http://localhost:8082
- **User Service**: http://localhost:8083

### Option 2: Local Development

#### Step 1: Start Infrastructure Services

```bash
# Start PostgreSQL and Redis using Docker Compose
docker-compose up -d postgres redis

# Or use local PostgreSQL and Redis instances
# Make sure PostgreSQL is running with databases: user_db, product_db, order_db
# Make sure Redis is running on localhost:6379
```

#### Step 2: Build the Project

```bash
# Build all modules
mvn clean install -DskipTests

# Or build specific service
mvn clean install -pl services/user-service -am
```

#### Step 3: Run Services

Run each service in a separate terminal:

```bash
# Terminal 1: User Service
cd services/user-service
mvn spring-boot:run

# Terminal 2: Product Service
cd services/product-service
mvn spring-boot:run

# Terminal 3: Order Service
cd services/order-service
mvn spring-boot:run

# Terminal 4: API Gateway
cd services/api-gateway
mvn spring-boot:run
```

#### Step 4: Verify Services

```bash
# Check API Gateway health
curl http://localhost:8080/actuator/health

# Check Swagger UI
open http://localhost:8080/swagger-ui.html
```

## ⚙️ Configuration

### Environment Variables

The application can be configured using environment variables:

#### API Gateway

```bash
# Server Configuration
SERVER_PORT=8080

# Service URLs
SERVICES_PRODUCT_SERVICE_URL=http://localhost:8081
SERVICES_ORDER_SERVICE_URL=http://localhost:8082
SERVICES_USER_SERVICE_URL=http://localhost:8083

# JWT Configuration
JWT_SECRET=your-secret-key-change-this-in-production-use-a-long-random-string-at-least-256-bits
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

#### Database Configuration

Each service requires database configuration:

```bash
# User Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/user_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Product Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/product_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Order Service
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/order_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

### Application Properties

Configuration files are located in `services/*/src/main/resources/application.yml`.

### Database Setup

The application uses Flyway for database migrations. Migrations are automatically applied on startup.

For manual database setup:

```sql
-- Create databases
CREATE DATABASE user_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
```

## 📚 API Documentation

### Interactive API Documentation

The API Gateway provides interactive Swagger documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Authentication

All endpoints (except login and registration) require JWT authentication.

#### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "USER"
  }
}
```

#### Using the Token

Include the token in the Authorization header:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Logout

```http
POST /api/auth/logout
Authorization: Bearer {token}
```

### API Endpoints

#### Authentication Endpoints

| Method | Endpoint           | Description | Auth Required |
| ------ | ------------------ | ----------- | ------------- |
| POST   | `/api/auth/login`  | User login  | No            |
| POST   | `/api/auth/logout` | User logout | Yes           |

#### User Endpoints

| Method | Endpoint                   | Description               | Roles |
| ------ | -------------------------- | ------------------------- | ----- |
| POST   | `/api/users`               | Create user               | All   |
| GET    | `/api/users`               | Get all users (paginated) | ADMIN |
| GET    | `/api/users/{id}`          | Get user by ID            | ADMIN |
| GET    | `/api/users/email/{email}` | Get user by email         | ADMIN |
| PUT    | `/api/users/{id}`          | Update user               | All   |
| DELETE | `/api/users/{id}`          | Delete user               | ADMIN |

#### Product Endpoints

| Method | Endpoint             | Description                 | Roles                     |
| ------ | -------------------- | --------------------------- | ------------------------- |
| POST   | `/api/products`      | Create product              | ADMIN                     |
| GET    | `/api/products`      | Search products (paginated) | USER, PREMIUM_USER, ADMIN |
| GET    | `/api/products/{id}` | Get product by ID           | USER, PREMIUM_USER, ADMIN |
| PUT    | `/api/products/{id}` | Update product              | ADMIN                     |
| DELETE | `/api/products/{id}` | Delete product              | ADMIN                     |

#### Order Endpoints

| Method | Endpoint                    | Description                                 | Roles                     |
| ------ | --------------------------- | ------------------------------------------- | ------------------------- |
| POST   | `/api/orders`               | Create order (with idempotency key support) | USER, PREMIUM_USER, ADMIN |
| GET    | `/api/orders`               | Get all orders (paginated)                  | USER, PREMIUM_USER, ADMIN |
| GET    | `/api/orders/{id}`          | Get order by ID                             | USER, PREMIUM_USER, ADMIN |
| GET    | `/api/orders/user/{userId}` | Get user orders                             | USER, PREMIUM_USER, ADMIN |

### Role-Based Access Control

#### USER
- View products
- Create and manage own orders
- Update own profile
- 5% discount on orders above $500

#### PREMIUM_USER
- All USER permissions
- 10% discount on all orders
- Additional 5% discount on orders above $500 (total 15%)

#### ADMIN
- Full CRUD on products
- Full CRUD on users
- View all orders
- All PREMIUM_USER permissions (10% discount + 5% for large orders)

### Discount System

The discount system uses the **Chain of Responsibility / Pipeline pattern** to apply multiple discount rules in a flexible and extensible way.

> 📖 **Detailed Documentation**: See [DISCOUNT_SYSTEM.md](services/order-service/DISCOUNT_SYSTEM.md) for comprehensive discount system documentation, code examples, and enhancement suggestions.

#### Architecture

- **DiscountRule Interface**: Defines the contract for discount rules
  - `isApplicable()`: Determines if the rule applies to the given order
  - `calculateDiscount()`: Calculates the discount amount
  - Rules are implemented as Spring `@Component` beans with `@Order` annotation for execution order

- **DiscountService**: Orchestrates discount calculation
  - Collects all `DiscountRule` beans via dependency injection
  - Filters applicable rules based on order subtotal and user role
  - Sums discounts from all applicable rules (additive)
  - Applies guardrails:
    - Discount cannot exceed order total
    - Discounts are rounded to 2 decimal places

#### Current Discount Rules

1. **LargeOrderExtraDiscountRule** (`@Order(10)`)
   - **Priority**: 10 (executed first)
   - **Applicable**: Orders above $500.00
   - **Discount**: 5% of order subtotal
   - **Applies to**: All user roles (USER, PREMIUM_USER, ADMIN)

2. **PremiumUserDiscountRule** (`@Order(20)`)
   - **Priority**: 20 (executed second)
   - **Applicable**: PREMIUM_USER or ADMIN roles
   - **Discount**: 10% of order subtotal
   - **Applies to**: PREMIUM_USER, ADMIN

#### Discount Examples

| User Role    | Order Amount | Large Order Rule | Premium Rule    | Total Discount |
| ------------ | ------------ | ---------------- | --------------- | -------------- |
| USER         | $100         | No (below $500)  | No              | $0             |
| USER         | $600         | Yes (5% = $30)   | No              | $30            |
| PREMIUM_USER | $100         | No               | Yes (10% = $10) | $10            |
| PREMIUM_USER | $600         | Yes (5% = $30)   | Yes (10% = $60) | $90 (15%)      |
| ADMIN        | $100         | No               | Yes (10% = $10) | $10            |
| ADMIN        | $600         | Yes (5% = $30)   | Yes (10% = $60) | $90 (15%)      |

#### Benefits of This Pattern

- **Extensibility**: Easy to add new discount rules without modifying existing code
- **Testability**: Each rule can be tested independently
- **Maintainability**: Clear separation of concerns
- **Flexibility**: Rules can be enabled/disabled or reordered via `@Order` annotation
- **Composability**: Multiple rules can apply simultaneously (discounts are additive)
- **Single Responsibility**: Each rule has one clear purpose

#### Adding a New Discount Rule

To add a new discount rule, simply:

1. Create a class implementing `DiscountRule`
2. Annotate with `@Component` and `@Order(priority)`
3. Implement `isApplicable()` and `calculateDiscount()` methods
4. Spring will automatically inject it into `DiscountService`

No changes needed to `DiscountService` or other rules!

See [DISCOUNT_SYSTEM.md](services/order-service/DISCOUNT_SYSTEM.md) for detailed examples and best practices.

### Example API Calls

#### Create a User

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }'
```

#### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

#### Create a Product (Admin)

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "quantity": 10
  }'
```

#### Create an Order

Orders are created with **CONFIRMED** status immediately, and product inventory is decreased automatically. You can optionally provide an `idempotencyKey` to prevent duplicate orders:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "userId": 1,
    "idempotencyKey": "unique-key-123",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

**Note**: 
- Orders are created with **CONFIRMED** status immediately (no separate confirmation step)
- Product inventory is automatically decreased upon order creation
- If you send the same request with the same `idempotencyKey` within 24 hours, the system will return the previously created order instead of creating a duplicate
- The `idempotencyKey` is optional; if not provided, each request will create a new order

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for a specific module
mvn test -pl services/user-service

# Run tests with coverage
mvn test jacoco:report
```

### Test Structure

- **Unit Tests**: Located in `src/test/java` using JUnit 5 and Mockito
- **Integration Tests**: Use Testcontainers for database testing

### Test Coverage

Each service includes:
- Unit tests for handlers, services, and controllers
- Integration tests for REST endpoints
- Validation tests for DTOs and commands

## 💻 Development

### Project Structure

```
ecomm/
├── shared/                    # Shared module (CQRS, DTOs, Commands)
│   └── src/
│       ├── main/java/com/aaami/
│       │   ├── cqrs/         # CQRS infrastructure
│       │   └── shared/       # Shared DTOs, commands, validators
│       └── test/
├── services/
│   ├── api-gateway/          # API Gateway service
│   ├── user-service/         # User management service
│   ├── product-service/      # Product catalog service
│   └── order-service/        # Order processing service
├── docker/                    # Docker configuration
├── docker-compose.yml         # Docker Compose configuration
├── Dockerfile                 # Multi-stage Dockerfile
└── pom.xml                    # Parent POM
```

### Development Workflow

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make Changes**
   - Follow existing code patterns
   - Write tests for new functionality
   - Update API documentation

3. **Run Tests**
   ```bash
   mvn clean test
   ```

4. **Build and Verify**
   ```bash
   mvn clean install
   ```

5. **Commit and Push**
   ```bash
   git commit -m "feat: your feature description"
   git push origin feature/your-feature-name
   ```

### Code Style

- Follow Java naming conventions
- Use Lombok for boilerplate code reduction
- Use Spring Boot best practices
- Write self-documenting code with meaningful names

### Adding a New Service

1. Create service module in `services/`
2. Add module to parent `pom.xml`
3. Create `Application.java` main class
4. Configure database and dependencies
5. Implement CQRS handlers
6. Add REST controllers
7. Write tests
8. Update `docker-compose.yml`

## 🚢 Deployment

### Docker Deployment

#### Build Individual Services

```bash
# Build specific service
docker build --build-arg MODULE=api-gateway -t ecomm-api-gateway .

# Or build all services
docker-compose build
```

#### Production Considerations

1. **Environment Variables**: Use secrets management (e.g., Kubernetes Secrets, AWS Secrets Manager)
2. **JWT Secret**: Use a strong, randomly generated secret (minimum 256 bits)
3. **Database**: Use managed database services (e.g., AWS RDS, Google Cloud SQL)
4. **Redis**: Use managed Redis (e.g., AWS ElastiCache, Redis Cloud)
5. **Load Balancing**: Use a load balancer for API Gateway
6. **Monitoring**: Implement logging, metrics, and tracing
7. **Health Checks**: Configure health check endpoints
8. **SSL/TLS**: Use HTTPS in production

### Kubernetes Deployment

Example Kubernetes deployment files can be created based on the Docker Compose configuration.

### CI/CD

Recommended CI/CD pipeline:

1. **Build**: Compile and run tests
2. **Test**: Run unit and integration tests
3. **Build Docker Images**: Create container images
4. **Security Scan**: Scan for vulnerabilities
5. **Deploy to Staging**: Deploy to staging environment
6. **Integration Tests**: Run end-to-end tests
7. **Deploy to Production**: Deploy to production

## 🔧 Troubleshooting

### Common Issues

#### Services Not Starting

**Problem**: Services fail to start

**Solutions**:
- Check database connectivity
- Verify Redis is running
- Check port availability
- Review application logs

#### Authentication Failures

**Problem**: JWT token validation fails

**Solutions**:
- Verify JWT secret matches across services
- Check token expiration
- Verify Redis session exists
- Check token format (Bearer prefix)

#### Database Connection Issues

**Problem**: Cannot connect to database

**Solutions**:
- Verify PostgreSQL is running
- Check database credentials
- Verify database exists
- Check network connectivity

#### Redis Connection Issues

**Problem**: Cannot connect to Redis

**Solutions**:
- Verify Redis is running
- Check Redis host and port
- Verify Redis password (if configured)
- Check network connectivity

### Logging

Application logs are available via:

```bash
# Docker Compose
docker-compose logs -f api-gateway

# Local
tail -f logs/application.log
```

### Health Checks

Check service health:

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# User Service
curl http://localhost:8083/actuator/health

# Product Service
curl http://localhost:8081/actuator/health

# Order Service
curl http://localhost:8082/actuator/health
```



---

**Built with ❤️ using Spring Boot, Docker, and modern microservices patterns.**

