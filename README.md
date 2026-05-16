# Expense Tracker cum Fraud Detection

A full-stack expense tracking application with fraud detection features. The project uses a React frontend, a Spring Boot backend, and MySQL for persistence. It is also containerised with Docker and ready for deployment.

## Features

- User-friendly expense tracking UI
- Fraud detection logic on the backend
- Spring Boot REST API
- JWT-based authentication
- MySQL database integration
- Dockerized local development and deployment
- Nginx-based frontend serving and reverse proxy routing
- Automated unit testing with JUnit 5 and Mockito
- Isolated H2 in-memory database testing setup

## Tech Stack

- **Frontend:** React + Vite
- **Backend:** Spring Boot 3
- **Database:** MySQL 8
- **Testing:** JUnit 5, Mockito, H2 Database
- **Containerization:** Docker, Docker Compose
- **Web Server:** Nginx
- **Language:** Java 17, JavaScript

## Project Structure

```text
.
├── backend
├── frontend
├── docker-compose.yml
├── .env.example
├── DEPLOYMENT.md
└── README.md
```

## Prerequisites

Before running locally, install:

- Docker
- Docker Compose plugin

Optional for non-Docker development:
- Java 17
- Node.js
- npm
- MySQL

## Environment Variables

Create a `.env` file in the project root.

Example:

```env
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=expense_tracker
MYSQL_USER=expense_user
MYSQL_PASSWORD=expense_password
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/expense_tracker?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=expense_user
SPRING_DATASOURCE_PASSWORD=expense_password
APP_JWT_SECRET=change-me-to-a-long-random-secret
APP_JWT_EXPIRATION_MS=86400000
```

## Run Locally with Docker

1. Copy the example environment file:

```bash
cp .env.example .env
```

2. Build and start the application:

```bash
docker compose up -d --build
```

3. Open the app:

- Frontend: `http://localhost`
- Backend: `http://localhost:8083`

# Automated Testing

The backend includes a complete unit testing setup using:

- JUnit 5
- Mockito
- H2 in-memory database

## Test Coverage

The test suite currently covers:

### Auth Service
- User registration
- Duplicate email validation
- Login authentication
- JWT token generation

### Transaction Service
- Transaction creation
- Transaction filtering
- User validation
- Repository interaction

### Fraud Detection Service
- High-value transaction detection
- Unusual debit pattern detection
- Excessive daily transaction checks
- Repeated round-number transaction checks

### AI Analysis Service
- Spending vs income analysis
- Savings insight generation
- Category-based analysis
- Unusual spend alerts
- Empty dataset handling

## Run Tests

```bash
cd backend
./mvnw clean test
```

## Test Database Configuration

Tests use a separate H2 in-memory database configuration located at:

```text
backend/src/test/resources/application.properties
```

This allows tests to run independently without requiring the MySQL Docker container.

---

## Stop the Application

```bash
docker compose down
```

## View Logs

```bash
docker compose logs -f
```

## Deployment on AWS EC2

1. Launch an Ubuntu EC2 instance.
2. Install Docker and the Docker Compose plugin.
3. Clone this repository on the instance.
4. Create the `.env` file.
5. Run:

```bash
docker compose up -d --build
```

Recommended Security Group rules:

- SSH: `22` from your IP only
- HTTP: `80` from the internet
- Do **not** expose MySQL or backend ports publicly

## Notes

- The frontend is served through Nginx.
- Nginx proxies API requests to the backend container.
- MySQL data is stored in a Docker volume.
- Keep `.env` out of version control.


## License

No license has been declared yet.
