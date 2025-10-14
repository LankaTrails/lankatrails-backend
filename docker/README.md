# Docker Configuration for LankaTrails Backend

This directory contains Docker configuration files for running the LankaTrails backend application with **profile-based configuration** and all its dependencies.

## 🏗️ **Profile-Based Architecture**

The application now uses **Spring Boot profiles** for different environments:

- **`dev`** - Local development with Docker services
- **`test`** - Testing with H2 in-memory database
- **`prod`** - Production with full services and Liquibase migrations

## Files Overview

- `docker-compose.yml` - Default development environment (uses `dev` profile)
- `docker-compose.dev.yml` - Complete development environment with `dev` profile
- `docker-compose.prod.yml` - Production environment with `prod` profile
- `.env` - Default environment variables (for local development)
- `.env.dev.example` - Template for development environment variables
- `.env.prod` - Production environment variables (git-ignored)
- `.env.prod.example` - Template for production environment variables
- `Dockerfile` - Application container build configuration

## 🚀 **Quick Start Guide**

### **Local Development (Recommended)**

Run locally without Docker for fastest development:

```bash
# Set up environment
export SPRING_PROFILES_ACTIVE=dev
# Or on Windows
set SPRING_PROFILES_ACTIVE=dev

# Run with Maven
mvn spring-boot:run -Dspring.profiles.active=dev

# Or run with your IDE and set active profile to 'dev'
```

### **Docker Development**

For full Docker development environment:

```bash
# Copy environment template (if needed)
cp .env.dev.example .env

# Run development stack
docker-compose up -d
# OR explicitly use dev compose file
docker-compose -f docker-compose.dev.yml --env-file .env up -d
```

### **Testing**

Run tests (automatically uses `test` profile with H2):

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=YourTestClass

# Run tests with different profile (if needed)
mvn test -Dspring.profiles.active=test
```

### **Production Deployment**

Deploy to production:

```bash
# Copy and customize production template
cp .env.prod.example .env.prod
# Edit .env.prod with your secure values

# Run production stack
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

## 📋 **Profile Details**

### **Development Profile (`dev`)**

- **Database**: PostgreSQL via Docker with `hibernate.ddl-auto=update`
- **Services**: Redis, MongoDB, RabbitMQ via Docker containers
- **Logging**: DEBUG level with SQL logging enabled
- **Features**: DevTools enabled, hot reload, full actuator endpoints
- **Usage**: Local development and Docker development

### **Test Profile (`test`)**

- **Database**: H2 in-memory with `hibernate.ddl-auto=create-drop`
- **Services**: No external services (mocked/disabled)
- **Logging**: INFO level (less verbose)
- **Features**: Minimal configuration for fast test execution
- **Usage**: Unit tests and integration tests

### **Production Profile (`prod`)**

- **Database**: PostgreSQL with `hibernate.ddl-auto=none`
- **Migrations**: Liquibase enabled for schema management
- **Services**: Redis, MongoDB, RabbitMQ via environment variables
- **Logging**: WARN level (production optimized)
- **Features**: Security enabled, limited actuator endpoints
- **Usage**: Production deployments

## 🔧 **Configuration Files**

### **Spring Boot Configuration**

- `application.yml` - Shared defaults across all profiles
- `application-dev.yml` - Development-specific settings
- `application-test.yml` - Test-specific settings
- `application-prod.yml` - Production-specific settings

### **Key Environment Variables**

#### Container Configuration

- `API_CONTAINER_NAME` - Application container name
- `API_IMAGE` - Application Docker image
- `API_PORT` - External port for the application
- `SERVER_PORT` - Internal application port

#### Database Configuration

- `DOCKER_DB_HOST`, `DOCKER_DB_PORT`, `DOCKER_DB_NAME` - Database connection settings
- `DOCKER_DB_USERNAME`, `DOCKER_DB_PASSWORD` - Database credentials

#### Redis Configuration

- `DOCKER_REDIS_HOST`, `DOCKER_REDIS_PORT` - Redis connection settings
- `DOCKER_REDIS_PASSWORD` - Redis password

#### RabbitMQ Configuration

- `DOCKER_RABBITMQ_HOST`, `DOCKER_RABBITMQ_PORT` - RabbitMQ connection settings
- `DOCKER_RABBITMQ_USERNAME`, `DOCKER_RABBITMQ_PASSWORD` - RabbitMQ credentials

## 🗄️ **Database Migrations**

### **Development (`dev` profile)**

- Uses `hibernate.ddl-auto=update` for automatic schema updates
- Good for rapid development and prototyping
- Schema changes are applied automatically

### **Production (`prod` profile)**

- Uses `hibernate.ddl-auto=none` with Liquibase migrations
- All schema changes must be in migration files
- Provides version control and rollback capabilities

### **Managing Migrations**

```bash
# Generate initial migration from existing entities (one-time setup)
mvn liquibase:generateChangeLog

# Create a new migration file
# Add to src/main/resources/db/changelog/changes/

# Apply migrations manually (if needed)
mvn liquibase:update

# Rollback migrations (if needed)
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

## 🚀 **Usage Examples**

### **Development Environment**

The development environment includes:

- All services with exposed ports for external access
- Debug logging enabled (`dev` profile)
- Development tools enabled
- Management endpoints fully exposed
- Volume mounts for hot reload

```bash
# Method 1: Local development (fastest)
mvn spring-boot:run -Dspring.profiles.active=dev

# Method 2: Docker development (full stack)
cd docker
docker-compose up -d

# Method 3: Explicit dev compose file
docker-compose -f docker-compose.dev.yml --env-file .env up -d

# View logs
docker-compose logs -f lankatrails-api

# Stop all services
docker-compose down
```

### Production Environment

The production environment includes:

- Optimized logging levels
- Resource limits
- Security-focused configuration
- No external port exposure for databases
- Restart policies

```bash
# Start all services in production mode
cd docker
docker-compose -f docker-compose.prod.yml --env-file .env.prod up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f lankatrails-api

# Stop all services
docker-compose -f docker-compose.prod.yml down
```

### Custom Environment

You can create custom environment configurations:

```bash
# Create custom environment file
cp .env.example .env.staging

# Edit .env.staging with your settings
# ...

# Start with custom environment
docker-compose --env-file .env.staging up -d
```

## Service Access

### Development Mode

- **Application**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **MongoDB**: localhost:27017
- **RabbitMQ Management**: http://localhost:15672 (admin/admin123)
- **Health Check**: http://localhost:8080/actuator/health

### Production Mode

- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- Databases are only accessible internally within the Docker network

## Database Initialization

PostgreSQL includes PostGIS extension and custom initialization scripts:

- `init-scripts/01-init.sql` - Initial database setup
- `init-scripts/02-postgis.sql` - PostGIS configuration

## Volumes

Data is persisted using Docker volumes:

- `lankatrails_postgres_data` - PostgreSQL data
- `lankatrails_redis_data` - Redis data
- `lankatrails_mongodb_data` - MongoDB data
- `lankatrails_rabbitmq_data` - RabbitMQ data
- `lankatrails_app_logs` - Application logs

## Health Checks

All services include health checks:

- **Application**: HTTP health endpoint
- **PostgreSQL**: `pg_isready` command
- **Redis**: `redis-cli ping` command
- **RabbitMQ**: `rabbitmq-diagnostics ping` command

## Troubleshooting

### View Service Status

```bash
# Development (default)
docker-compose ps

# Production
docker-compose -f docker-compose.prod.yml ps
```

### View Service Logs

```bash
# Development - All services
docker-compose logs

# Development - Specific service
docker-compose logs lankatrails-api

# Production - Follow logs
docker-compose -f docker-compose.prod.yml logs -f
```

### Restart Services

```bash
# Development - Restart all services
docker-compose restart

# Production - Restart specific service
docker-compose -f docker-compose.prod.yml restart lankatrails-api
```

### Clean Up

```bash
# Development - Stop and remove containers
docker-compose down

# Production - Remove volumes (WARNING: This will delete all data)
docker-compose -f docker-compose.prod.yml down -v

# Remove images
docker-compose down --rmi all
```

## Environment Variables Reference

For a complete list of available environment variables, see:

- `.env.example` - Template with all development environment variables
- `.env.prod.example` - Template with all production environment variables
- `.env` - Your local development environment (git-ignored)
- `.env.prod` - Your production environment (git-ignored)

The variables are organized by service and functionality in all files.

## Security Notes

- Change default passwords in production
- Use environment-specific `.env` files
- Don't expose database ports in production
- Use proper firewall rules
- Regularly update Docker images
- Monitor resource usage and adjust limits as needed
