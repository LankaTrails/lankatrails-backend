# LankaTrails Backend - Profile-Based Configuration

This Spring Boot application uses profile-based configuration to manage different environments.

## Available Profiles

### 1. Development Profile (`dev`)

- **Purpose**: Local development with Docker services
- **Configuration**: `application-dev.yml`
- **Database**: PostgreSQL with PostGIS (via Docker)
- **External Services**: Redis, MongoDB, RabbitMQ (via Docker)
- **Features**: Debug logging, H2 console enabled, detailed error messages

### 2. Production Profile (`prod`)

- **Purpose**: Production deployment
- **Configuration**: `application-prod.yml`
- **Database**: External PostgreSQL with Liquibase migrations
- **External Services**: External Redis, MongoDB, RabbitMQ
- **Features**: Optimized logging, security hardened, performance tuned

### 3. Test Profile (`test`)

- **Purpose**: Unit and integration testing
- **Configuration**: `application-test.yml`
- **Database**: H2 in-memory database
- **External Services**: Disabled/mocked
- **Features**: Fast startup, isolated testing environment

## Running the Application

### Development Environment

```bash
# Start infrastructure services
docker-compose -f docker-compose.dev.yml up -d

# Run application with dev profile
mvn spring-boot:run -Dspring.profiles.active=dev

# Or using Java directly
java -Dspring.profiles.active=dev -jar target/lankatrails-backend-*.jar
```

### Production Environment

```bash
# Set up production environment variables
cp .env.prod.example .env.prod
# Edit .env.prod with actual production values

# Start production services
docker-compose -f docker-compose.prod.yml up -d

# Run application with prod profile
java -Dspring.profiles.active=prod -jar target/lankatrails-backend-*.jar
```

### Testing

```bash
# Run tests (automatically uses test profile)
mvn test

# Run with specific test profile
mvn test -Dspring.profiles.active=test
```

## Environment Variables

### Development (.env)

Copy `.env.dev.example` to `.env` and customize as needed for local development.

### Production (.env.prod)

Copy `.env.prod.example` to `.env.prod` and set actual production values:

- Database credentials
- Redis authentication
- MongoDB connection
- RabbitMQ credentials
- JWT secrets
- Stripe keys
- SMTP configuration

## Docker Compose Files

### docker-compose.dev.yml

- PostgreSQL with PostGIS
- Redis for caching
- MongoDB for data storage
- RabbitMQ for messaging
- All services exposed for development

### docker-compose.prod.yml

- Production-optimized configurations
- Resource limits applied
- Security hardened
- Limited port exposure

## Database Migrations

Production uses Liquibase for database migrations:

- **Changelog**: `src/main/resources/db/changelog/db.changelog-master.xml`
- **Migration Files**: `src/main/resources/db/changelog/changes/`

### Running Migrations Manually

```bash
# Update database to latest version
mvn liquibase:update

# Generate change log from existing database
mvn liquibase:generateChangeLog

# Roll back last change
mvn liquibase:rollback -Dliquibase.rollbackCount=1
```

## Configuration Hierarchy

1. **application.yml** - Base configuration shared across all profiles
2. **application-{profile}.yml** - Profile-specific overrides
3. **Environment Variables** - Runtime configuration (highest priority)
4. **Command Line Arguments** - Override any configuration

## Security Notes

- Never commit actual credentials to version control
- Use `.env` files for local development only
- Production credentials should be managed via secure deployment pipelines
- All example files contain placeholder values only

## Troubleshooting

### Profile Not Loading

- Ensure `spring.profiles.active` is set correctly
- Check that the profile-specific YAML file exists
- Verify YAML syntax is valid

### Docker Services Not Starting

- Check if ports are already in use
- Verify Docker is running
- Check environment variable values in `.env` files

### Database Connection Issues

- Ensure PostgreSQL is running (check docker-compose logs)
- Verify connection parameters in profile configuration
- Check network connectivity between services
