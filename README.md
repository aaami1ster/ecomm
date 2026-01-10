# E-Commerce Microservices Platform

A modern, scalable e-commerce platform built with Spring Boot microservices architecture, featuring API versioning, resilience patterns (circuit breaker & retry), event-driven architecture with Kafka, Redis caching, stateless JWT authentication, and comprehensive API documentation.

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
- [Recent Updates](#recent-updates)
- [Future Enhancements & TODOs](#future-enhancements--todos)

## 🎯 Overview

This e-commerce platform is a multi-module Spring Boot application implementing a microservices architecture. It provides a complete solution for managing users, products, and orders with role-based access control and secure authentication.

### Key Features

- **Microservices Architecture**: Independent, scalable services for users, products, and orders
- **API Versioning**: URL-based versioning (`/api/v1/...`) with backward compatibility support
- **Stateless JWT Authentication**: Secure token-based authentication without server-side session storage
- **Role-Based Access Control (RBAC)**: Three-tier role system (USER, PREMIUM_USER, ADMIN)
- **Resilience Patterns**: Circuit breaker and retry mechanisms for inter-service communication
- **Event-Driven Architecture**: Kafka-based event streaming for service communication
- **Redis Caching**: Product caching for hot reads and rate limiting
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
        └─────────────┘
               │
               │
        ┌──────▼──────┐
        │   Kafka     │
        │   Broker    │
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
  - Role-based access control enforcement
  - API versioning (`/api/v1/...`)
  - Rate limiting (login endpoint)
  - API documentation (Swagger/OpenAPI)
  - Circuit breaker and retry for downstream services

#### User Service (Port 8083)
- **Purpose**: User management and authentication
- **Responsibilities**:
  - User CRUD operations
  - User role management (USER, PREMIUM_USER, ADMIN)
  - User profile management
  - BCrypt password encoding
  - Password verification endpoint
  - Kafka event publishing (user lifecycle events)
  - Database: `user_db`

#### Product Service (Port 8081)
- **Purpose**: Product catalog management
- **Responsibilities**:
  - Product CRUD operations
  - Product inventory management
  - Product search and filtering
  - Redis caching for hot reads
  - Kafka event publishing (product lifecycle events)
  - Database: `product_db`

#### Order Service (Port 8082)
- **Purpose**: Order processing and management
- **Responsibilities**:
  - Order creation and management
  - Order status tracking
  - Discount calculation based on user roles
  - Inventory synchronization with Product Service (with circuit breaker and retry)
  - Idempotency key support for order creation
  - Kafka event publishing (order lifecycle events)
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

### 3. Stateless JWT Authentication

**Decision**: Pure stateless JWT authentication without server-side session storage

**Rationale**:
- **True Statelessness**: No server-side session storage required - all authentication information is in the JWT token itself
- **Scalability**: No need to share session state across servers, enabling horizontal scaling without sticky sessions
- **Performance**: No Redis lookups required for authentication - token validation is purely cryptographic
- **Simplicity**: Simpler architecture with fewer moving parts and dependencies
- **Microservices Friendly**: Each service can validate tokens independently without shared state
- **Client-Side Logout**: Logout is handled client-side by discarding the token (logout endpoint exists for API consistency)
- **Token Expiration**: Security is maintained through token expiration times rather than server-side revocation

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

### 9. API Versioning

**Decision**: URL-based API versioning with backward compatibility

**Rationale**:
- **Future-Proof**: Enables API evolution without breaking existing clients
- **Clear Migration Path**: Clients can migrate to new versions at their own pace
- **Backward Compatibility**: Non-versioned paths (`/api/...`) are still supported but deprecated
- **Consistency**: Centralized version constants ensure consistency across all endpoints
- **Industry Standard**: URL-based versioning is widely adopted and easy to understand

**Implementation**:
- All endpoints support both `/api/v1/...` (recommended) and `/api/...` (deprecated)
- Version constants defined in `ApiVersion` class for maintainability
- OpenAPI documentation reflects API versioning
- Rate limiting and security configurations support both versioned and non-versioned paths

**Example**:
```
POST /api/v1/auth/login     (Recommended)
POST /api/auth/login        (Deprecated, but still supported)
```

### 10. Resilience Patterns (Circuit Breaker & Retry)

**Decision**: Resilience4j for circuit breaker and retry patterns in inter-service communication

**Rationale**:
- **Fault Tolerance**: Prevents cascading failures when downstream services are unavailable
- **Improved User Experience**: Automatic retries for transient failures
- **Resource Protection**: Circuit breaker prevents overwhelming failing services
- **Observability**: Health indicators and metrics for monitoring
- **Configurable**: Fine-tuned settings per service (product, user, order)

**Implementation Details**:

**Circuit Breaker**:
- **Failure Rate Threshold**: 50% (opens circuit when 50% of calls fail)
- **Sliding Window Size**: 10 calls
- **Minimum Calls**: 5 calls before circuit can open
- **Wait Duration**: 10 seconds in open state before attempting half-open
- **Half-Open State**: Allows 3 test calls before fully closing
- **Fallback Methods**: Graceful degradation with meaningful error messages

**Retry**:
- **Max Attempts**: 3 retries for transient failures
- **Wait Duration**: 1 second initial delay
- **Exponential Backoff**: Enabled with multiplier of 2
- **Retryable Exceptions**: Network errors, connection timeouts, server errors

**Services Using Resilience**:
- **Order Service**: Circuit breaker and retry for Product Service and User Service calls
- **API Gateway**: Circuit breaker and retry for all downstream service calls

**Benefits**:
- **Resilience**: System continues operating even when some services are down
- **Performance**: Automatic retries handle transient network issues
- **Monitoring**: Health indicators show circuit breaker status
- **User Experience**: Better error messages instead of generic failures

### 11. Event-Driven Architecture with Kafka

**Decision**: Kafka for asynchronous event-driven communication between services

**Rationale**:
- **Decoupling**: Services communicate via events, reducing tight coupling
- **Scalability**: Event streaming allows horizontal scaling
- **Reliability**: Kafka provides durability and message ordering
- **Event Sourcing Ready**: Foundation for event sourcing patterns
- **Audit Trail**: All events are persisted for audit and replay

**Event Types**:
- **User Events**: `user-created`, `user-updated`, `user-deleted`
- **Product Events**: `product-created`, `product-updated`, `product-deleted`, `inventory-decreased`
- **Order Events**: `order-created`, `order-status-changed`

**Configuration**:
- Kafka broker accessible at `broker:29092` in Docker environment
- Topics auto-created on first use
- JSON serialization for event payloads
- Idempotent producers enabled

### 12. Redis Caching Strategy

**Decision**: Cache-aside pattern for product data with TTL-based expiration

**Rationale**:
- **Performance**: Sub-millisecond read times for frequently accessed products
- **Reduced Database Load**: Caching hot reads reduces database queries
- **Scalability**: Redis can handle high-throughput cache operations
- **TTL Management**: Automatic cache expiration (30 minutes default)
- **Cache Invalidation**: Smart invalidation on product updates/deletes

**Implementation**:
- Product reads check cache first, then database
- Cache miss: fetch from database and populate cache
- Cache hit: return cached data immediately
- Updates/deletes invalidate relevant cache entries
- Graceful degradation: system works even if Redis is unavailable

### 13. Rate Limiting

**Decision**: Redis-based rate limiting for login endpoint using sliding window algorithm

**Rationale**:
- **Security**: Prevents brute-force attacks on authentication
- **Resource Protection**: Limits abuse of login endpoint
- **Configurable**: Adjustable limits via environment variables
- **Distributed**: Works across multiple gateway instances

**Configuration**:
- **Default**: 5 login attempts per 15-minute window
- **Key**: Client IP address
- **Response**: 429 Too Many Requests with retry-after header
- **Headers**: X-RateLimit-Limit, X-RateLimit-Remaining

### 14. Idempotency Key for Order Creation

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
- **Kafka UI** (available at http://localhost:8090) for Kafka topic management

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
- **Kafka UI**: http://localhost:8090 (admin/pass)

### Default User Credentials

The following test users are available for testing (password for all: `password123`):

| Email                 | Password    | Role         | Permissions                                                            |
| --------------------- | ----------- | ------------ | ---------------------------------------------------------------------- |
| `admin@example.com`   | password123 | ADMIN        | Full access, can view all orders, manage products/users                |
| `user@example.com`    | password123 | USER         | Can create orders, view own orders, 5% discount on orders >$500        |
| `premium@example.com` | password123 | PREMIUM_USER | Can create orders, view own orders, 10% discount + 5% for large orders |

**Example Login**:

```bash
# Login as admin
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password123"
  }'

# Login as regular user
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Login as premium user
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "premium@example.com",
    "password": "password123"
  }'
```

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

# Test login with default user
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Default Test Users** (password for all: `password123`):
- `admin@example.com` - ADMIN role (full access)
- `user@example.com` - USER role (can create orders)
- `premium@example.com` - PREMIUM_USER role (10% discount + 5% for large orders)

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

# Rate Limiting Configuration
RATE_LIMIT_LOGIN_MAX=5
RATE_LIMIT_LOGIN_WINDOW=15  # minutes

# Kafka Configuration
SPRING_KAFKA_BOOTSTRAP_SERVERS=broker:29092
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

### Kafka Configuration

Kafka is used for event-driven communication between services. Configuration is done via environment variables:

```bash
# Kafka Bootstrap Servers
SPRING_KAFKA_BOOTSTRAP_SERVERS=broker:29092  # Docker environment
# SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092  # Local development

# Topic Names (optional, defaults provided)
KAFKA_TOPIC_USER_CREATED=user-created
KAFKA_TOPIC_USER_UPDATED=user-updated
KAFKA_TOPIC_USER_DELETED=user-deleted
KAFKA_TOPIC_PRODUCT_CREATED=product-created
KAFKA_TOPIC_PRODUCT_UPDATED=product-updated
KAFKA_TOPIC_PRODUCT_DELETED=product-deleted
KAFKA_TOPIC_INVENTORY_DECREASED=inventory-decreased
KAFKA_TOPIC_ORDER_CREATED=order-created
KAFKA_TOPIC_ORDER_STATUS_CHANGED=order-status-changed
```

**Note**: Topics are auto-created on first use. For production, consider pre-creating topics with appropriate partitions and replication factors.

## 📚 API Documentation

### Interactive API Documentation

The API Gateway provides interactive Swagger documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### API Versioning

All API endpoints support versioned paths. The recommended approach is to use `/api/v1/...` paths:

- **Versioned (Recommended)**: `/api/v1/auth/login`, `/api/v1/users`, `/api/v1/products`, `/api/v1/orders`
- **Non-Versioned (Deprecated)**: `/api/auth/login`, `/api/users`, `/api/products`, `/api/orders`

Non-versioned paths are still supported for backward compatibility but are deprecated. New clients should use versioned paths.

### Authentication

All endpoints (except login and registration) require JWT authentication.

#### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Rate Limiting**: Login endpoint is rate-limited to 5 attempts per 15 minutes per IP address. Exceeding the limit returns `429 Too Many Requests`.

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
POST /api/v1/auth/logout
Authorization: Bearer {token}
```

### API Endpoints

> **Note**: All endpoints support both `/api/v1/...` (recommended) and `/api/...` (deprecated) paths. Examples below use versioned paths.

#### Authentication Endpoints

| Method | Endpoint              | Description | Auth Required |
| ------ | --------------------- | ----------- | ------------- |
| POST   | `/api/v1/auth/login`  | User login  | No            |
| POST   | `/api/v1/auth/logout` | User logout | Yes           |

#### User Endpoints

| Method | Endpoint                      | Description               | Roles |
| ------ | ----------------------------- | ------------------------- | ----- |
| POST   | `/api/v1/users`               | Create user               | All   |
| GET    | `/api/v1/users`               | Get all users (paginated) | ADMIN |
| GET    | `/api/v1/users/{id}`          | Get user by ID            | ADMIN |
| GET    | `/api/v1/users/email/{email}` | Get user by email         | ADMIN |
| PUT    | `/api/v1/users/{id}`          | Update user               | All   |
| DELETE | `/api/v1/users/{id}`          | Delete user               | ADMIN |

#### Product Endpoints

| Method | Endpoint                | Description                 | Roles                     |
| ------ | ----------------------- | --------------------------- | ------------------------- |
| POST   | `/api/v1/products`      | Create product              | ADMIN                     |
| GET    | `/api/v1/products`      | Search products (paginated) | USER, PREMIUM_USER, ADMIN |
| GET    | `/api/v1/products/{id}` | Get product by ID           | USER, PREMIUM_USER, ADMIN |
| PUT    | `/api/v1/products/{id}` | Update product              | ADMIN                     |
| DELETE | `/api/v1/products/{id}` | Delete product              | ADMIN                     |

#### Order Endpoints

| Method | Endpoint                       | Description                                 | Roles                     |
| ------ | ------------------------------ | ------------------------------------------- | ------------------------- |
| POST   | `/api/v1/orders`               | Create order (with idempotency key support) | USER, PREMIUM_USER        |
| GET    | `/api/v1/orders`               | Get all orders (paginated)                  | USER, PREMIUM_USER, ADMIN |
| GET    | `/api/v1/orders/{id}`          | Get order by ID                             | USER, PREMIUM_USER, ADMIN |
| GET    | `/api/v1/orders/user/{userId}` | Get user orders                             | USER, PREMIUM_USER, ADMIN |

> **Note**: ADMIN role can view orders but cannot create them.

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
   - **Applies to**: All user roles (USER, PREMIUM_USER)

2. **PremiumUserDiscountRule** (`@Order(20)`)
   - **Priority**: 20 (executed second)
   - **Applicable**: PREMIUM_USER role
   - **Discount**: 10% of order subtotal
   - **Applies to**: PREMIUM_USER

#### Discount Examples

| User Role    | Order Amount | Large Order Rule | Premium Rule    | Total Discount |
| ------------ | ------------ | ---------------- | --------------- | -------------- |
| USER         | $100         | No (below $500)  | No              | $0             |
| USER         | $600         | Yes (5% = $30)   | No              | $30            |
| PREMIUM_USER | $100         | No               | Yes (10% = $10) | $10            |
| PREMIUM_USER | $600         | Yes (5% = $30)   | Yes (10% = $60) | $90 (15%)      |

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
curl -X POST http://localhost:8080/api/v1/users \
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
# Login with default test user
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Response contains JWT token
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "type": "Bearer",
#   "user": {
#     "id": 1,
#     "email": "user@example.com",
#     "role": "USER"
#   }
# }
```

**Available Test Users**:
- `admin@example.com` / `password123` - ADMIN role
- `user@example.com` / `password123` - USER role  
- `premium@example.com` / `password123` - PREMIUM_USER role

#### Create a Product (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/products \
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
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "idempotencyKey": "unique-key-123",
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

> **Note**: The `userId` is automatically extracted from the JWT token. USER and PREMIUM_USER can only view their own orders, while ADMIN can view all orders.

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
- Verify JWT token is valid and not expired
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

#### Kafka Connection Issues

**Problem**: Services cannot connect to Kafka broker

**Solutions**:
- Verify Kafka broker is running: `docker compose ps broker`
- Check `SPRING_KAFKA_BOOTSTRAP_SERVERS` environment variable is set to `broker:29092`
- Verify Kafka broker health: `docker compose logs broker`
- Check network connectivity between services and broker
- Ensure Kafka topics are created (auto-created on first use)

#### Circuit Breaker Open

**Problem**: Circuit breaker is open, requests failing immediately

**Solutions**:
- Check downstream service health
- Review circuit breaker metrics: `/actuator/health`
- Wait for circuit breaker to transition to half-open state (10 seconds default)
- Verify service endpoints are responding correctly

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

# Kafka Broker
docker compose logs broker --tail 20
```

### Resilience Monitoring

Monitor circuit breaker and retry metrics:

```bash
# Circuit breaker health indicators
curl http://localhost:8082/actuator/health | jq '.components.circuitBreakers'

# Retry metrics
curl http://localhost:8082/actuator/metrics/resilience4j.retry.calls | jq
```



---

## 🔄 Recent Updates

### API Versioning
- All endpoints now support versioned paths (`/api/v1/...`)
- Backward compatibility maintained for non-versioned paths
- Centralized version constants for maintainability

### Resilience Patterns
- Circuit breaker implementation for inter-service communication
- Automatic retry with exponential backoff
- Graceful degradation with meaningful error messages
- Health indicators for monitoring

### Event-Driven Architecture
- Kafka integration for asynchronous event publishing
- Event types: user, product, and order lifecycle events
- Kafka UI available at http://localhost:8090

### Performance Optimizations
- Redis caching for product hot reads
- Cache-aside pattern with TTL-based expiration
- Rate limiting for login endpoint

### Security Enhancements
- BCrypt password encoding
- Rate limiting to prevent brute-force attacks
- Improved error handling and logging

## 🚀 Future Enhancements & TODOs

### High Priority

#### 1. Event-Driven State Changes (Most Important)
**Current State**: Order service makes synchronous HTTP calls to Product service to decrease inventory.

**Enhancement**: Implement full event-driven architecture for state changes:

- **Order Service**: 
  - Publish `order-created` event when order is placed
  - Publish `order-status-changed` event when order status updates
  - Remove direct HTTP calls to Product Service

- **Product Service**:
  - Consume `order-created` events from Kafka
  - Automatically decrease product inventory based on order items
  - Publish `inventory-decreased` event after successful update
  - Handle event processing failures with retry and dead-letter queue

- **Benefits**:
  - **True Decoupling**: Services communicate only via events, no direct dependencies
  - **Better Scalability**: Asynchronous processing allows better throughput
  - **Resilience**: Event replay capability for recovery from failures
  - **Eventual Consistency**: System remains available even if one service is temporarily down
  - **Audit Trail**: Complete history of all state changes via events

**Example Flow** (with Event Outbox Pattern):
```
1. User creates order → Order Service
2. Order Service starts database transaction
3. Order Service saves order to database
4. Order Service saves "order-created" event to outbox table (same transaction)
5. Transaction commits (order + event both persisted atomically)
6. Outbox processor polls outbox table and publishes event to Kafka
7. Product Service consumes "order-created" event
8. Product Service decreases inventory for ordered products
9. Product Service publishes "inventory-decreased" event (via outbox)
10. Order Service can optionally consume "inventory-decreased" for confirmation
```

**Implementation Considerations**:
- **Event Outbox Pattern** (Critical): 
  - Store events in an "outbox" table within the same database transaction as business data
  - Use a separate process (e.g., Debezium, custom poller) to read from outbox and publish to Kafka
  - Ensures **transactional consistency**: Events are only published if the business transaction commits
  - Prevents **lost events**: Events are persisted even if Kafka is temporarily unavailable
  - Guarantees **at-least-once delivery**: Outbox ensures events are eventually published
  - **Example Flow**:
    ```
    1. Order Service receives order creation request
    2. Start database transaction
    3. Save order to orders table
    4. Save "order-created" event to outbox table (same transaction)
    5. Commit transaction (both order and event are persisted)
    6. Outbox processor polls outbox table
    7. Outbox processor publishes event to Kafka
    8. Mark event as published in outbox table
    9. Product Service consumes event and decreases inventory
    ```
  - **Benefits**:
    - **ACID Guarantees**: Event publishing is part of the business transaction
    - **Reliability**: No events lost even during Kafka outages
    - **Consistency**: Business data and events are always in sync
    - **Idempotency**: Outbox processor can safely retry failed publishes
- Use Kafka consumer groups for reliable event processing
- Implement idempotent event handlers to prevent duplicate processing
- Add event versioning for schema evolution
- Implement saga pattern for distributed transactions if needed
- Add dead-letter queue for failed events
- Consider event sourcing for complete audit trail

### Medium Priority

#### 2. Event Consumers for Cross-Service Updates
- User Service: Consume product events to maintain user preferences cache
- Order Service: Consume user events to update order user information
- Product Service: Consume order events for analytics and reporting

#### 3. Distributed Tracing
- Implement distributed tracing (e.g., Spring Cloud Sleuth, OpenTelemetry)
- Track requests across all services
- Correlate events with originating requests

#### 4. API Gateway Enhancements
- Request/response transformation
- API aggregation (combine data from multiple services)
- Request caching at gateway level
- API analytics and monitoring

#### 5. Advanced Caching Strategies
- Cache warming on service startup
- Cache invalidation via events (event-driven cache invalidation)
- Multi-level caching (local + Redis)

#### 6. Enhanced Monitoring & Observability
- Prometheus metrics collection
- Grafana dashboards
- Alerting for circuit breaker states
- Business metrics (orders per hour, revenue, etc.)

### Low Priority

#### 7. API v2 Planning
- Design API v2 with breaking changes
- Migration strategy from v1 to v2
- Deprecation timeline for v1

#### 8. GraphQL API
- Add GraphQL endpoint for flexible data fetching
- Reduce over-fetching and under-fetching
- Better mobile client support

#### 9. WebSocket Support
- Real-time order status updates
- Live inventory notifications
- Real-time notifications for users

#### 10. Advanced Security
- OAuth2 integration
- API key management
- Rate limiting per user/API key
- Request signing for additional security

---

**Built with ❤️ using Spring Boot, Docker, Kafka, Redis, and modern microservices patterns.**

