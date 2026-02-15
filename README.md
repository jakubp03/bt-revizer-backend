# VBAP Project

This is a Spring Boot application for project and task management with JWT-based authentication.

## Prerequisites

- Java 21
- Maven 3.6+
- Docker and Docker Compose (for containerized setup)
- MySQL database

## Setup

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd opr3
   ```

2. Configure environment variables:
   - Copy `application-dev.properties.example` to `application-dev.properties` and fill in the required values (database credentials, JWT secrets).
   - For Docker, copy `.env.example` to `.env` and configure the variables.

3. Set up the database:
   - Ensure MySQL is running.
   - The application will create tables automatically on startup.

## Running the Application

### Local Development (with Maven)

1. Build the project:
   ```bash
   ./mvnw clean install
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. The application will be available at `http://localhost:8080`.


## API Endpoints

### Authentication (`/api/auth`)
- `POST /authenticate` - Login with email and password
- `POST /register` - Register a new user
- `POST /refresh` - Refresh access token using refresh token cookie
- `POST /logout` - Logout and revoke tokens
- `GET /validateToken` - Validate current access token

### Projects (`/api/projects`)
- `GET /` - Get all projects for authenticated user
- `POST /` - Create a new project
- `GET /{projectId}` - Get project by ID
- `PUT /{projectId}` - Update project details
- `PATCH /{projectId}/archive` - Archive a project
- `POST /{projectId}/users` - Add user to project by email

### Tasks (`/api/projects/{projectId}/tasks`)
- `POST /` - Create a new task in project
- `GET /{taskId}` - Get task by ID
- `GET /` - Get all tasks for project
- `GET /paginated?page={page}&size={size}` - Get paginated tasks for project
- `PUT /{taskId}` - Update task details
- `PATCH /{taskId}/status` - Update task status only
- `DELETE /{taskId}` - Delete a task



## Configuration

### Profiles
- `dev` - Local development with external MySQL
- `test` - Test environment with test-specific configuration

### Key Configuration Files
- [`application.properties`](target/classes/application.properties) - Common configuration
- [`application-dev.properties`](target/classes/application-dev.properties) - Development-specific settings




## Project Structure

```
src/
├── main/
│   ├── java/com/opr3/opr3/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Custom exceptions
│   │   ├── filter/          # Security filters
│   │   ├── repository/      # JPA repositories
│   │   ├── service/         # Business logic
│   │   └── util/            # Utility classes
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties.example
│       └── logback-spring.xml
└── test/
    └── java/com/opr3/opr3/  # Test classes
```

## Error Handling

The application uses a global exception handler ([`GlobalExceptionHandler`](src/main/java/com/opr3/opr3/exception/GlobalExceptionHandler.java)) that returns standardized error responses:

```json
{
  "status": 400,
  "message": "Error description",
  "timestamp": "2024-01-01T12:00:00",
  "path": "/api/endpoint"
}
```

Common HTTP status codes:
- `400` - Bad Request (invalid input)
- `401` - Unauthorized (authentication required/failed)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found (resource doesn't exist)
- `409` - Conflict (duplicate resource/illegal state)

## Logging

The application uses SLF4J with Logback for logging. Logs include:
- Request path, method, and client IP
- Username (for authenticated requests)
- HTTP status codes

Log files are stored in the `logs/` directory with daily rotation.