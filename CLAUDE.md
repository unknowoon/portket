# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Build the application
```bash
./gradlew clean build
```

### Run tests
```bash
./gradlew test
```

### Run the application locally
```bash
./gradlew bootRun
```

### Generate JOOQ classes (auto-runs during build)
```bash
./gradlew generateJooq
```

### Docker commands
```bash
# Build Docker image
docker build -t portket:latest .

# Run container (development)
docker run -d --name portket-dev --network portket -p 8008:8080 portket:latest

# Run container (production)
docker run -d --name portket-prod --network portket -p 8080:8080 portket:latest
```

## Architecture Overview

This is a Spring Boot financial portfolio management application that follows Domain-Driven Design principles with a layered architecture.

### Key Architectural Decisions

1. **Dual Query Strategy**: JPA repositories for standard CRUD operations, JOOQ for complex type-safe queries
2. **Event-Driven Architecture**: Kafka integration for asynchronous processing through consumer classes
3. **Security**: OAuth2 (Google) authentication with JWT tokens for stateless session management
4. **API Documentation**: Swagger/OpenAPI available at `/swagger-ui.html`

### Package Structure

```
com.portket.app/
├── controller/     # REST endpoints - all APIs under /api/*
├── service/        # Business logic layer
├── repository/     # Data access (JPA + JOOQ)
├── domain/         # JPA entities representing business concepts
├── dto/            # Data transfer objects for API communication
├── consumer/       # Kafka message consumers
└── finder/         # Custom query interfaces
```

### Core Domain Model

The application manages financial portfolios with these key entities:
- **User**: Core user entity linked to OAuth2 authentication
- **Portfolio**: Investment portfolios owned by users
- **Instrument**: Financial instruments (stocks, bonds, etc.)
- **Transaction**: Buy/sell records with amounts and prices
- **Holding**: Current positions calculated from transactions
- **Balance**: Account balance tracking
- **ExchangeRate**: Currency conversion support
- **Tag**: Flexible categorization system

### Database Configuration

The application uses PostgreSQL. Database connection details are configured via environment variables (see `.env.example`). JOOQ code generation reads these settings automatically during build.

### Environment Strategy

- **Development**: Deploys to port 8008 (container: portket-dev)
- **Production**: Deploys to port 8080 (container: portket-prod)
- **Network**: Uses Docker network named "portket"
- **Database**: PostgreSQL (configure via DB_URL environment variable)
- **Kafka**: Bootstrap server (configure via KAFKA_BOOTSTRAP_SERVERS environment variable)

### Jenkins CI/CD Pipeline

The Jenkinsfile defines automated deployment:
- PRs to `dev` branch deploy to staging (port 8008)
- PRs to `main` branch deploy to production (port 8080)
- Automatic Docker image building and container replacement

### API Endpoints Structure

All APIs follow RESTful conventions under `/api/*`:
- `/api/auth/*` - Authentication endpoints
- `/api/users/*` - User management
- `/api/portfolios/*` - Portfolio CRUD
- `/api/instruments/*` - Instrument management
- `/api/transactions/*` - Transaction recording
- `/api/holdings/*` - Holdings queries
- `/api/balances/*` - Balance operations
- `/api/tags/*` - Tag management

### Security Configuration

- Public endpoints: `/swagger-ui/**`, `/api/auth/**`, `/health`
- All other endpoints require JWT authentication
- CORS configured for local development and production domains
- JWT tokens issued after successful OAuth2 authentication
