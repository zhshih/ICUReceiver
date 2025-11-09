# ICU Receiver — Spring Boot Web Application

A demo Spring Boot web application for receiving and managing ICU (Intensive Care Unit) signal data in real-time.  
The system is designed with **resilience**, **observability**, and **modern Spring Boot 3** practices.

> This project is intended to run **in pair with [ICUSimulator](https://github.com/your-org/ICUSimulator)**, which streams simulated ICU signals via **WebSocket** to this receiver.

## Features
* Built with **Spring Boot 3** for modern, efficient development
* **Spring Data JPA** for database persistence
* **Resilience** features to handle faults gracefully
* **Observability** including logging, metrics, and tracing
* ...WebSocket...
* RESTful APIs for managing books

## APIs

## Technology Stack

## Getting Started

1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```

## Getting Started

1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```

2. Build the project using Maven or Gradle:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

Access the APIs at http://localhost:8080/api/v1/books.

## Observability & Resilience
* Metrics, logs, and tracing are enabled for monitoring.
* Resilience patterns like retries, circuit breakers, and rate limiting are applied where appropriate.

### Available Actuator Endpoints

| Endpoint | Description |
|-----------|-------------|
| `/actuator/health` | Shows application health status (DB connection, disk space, etc.) |
| `/actuator/info` | Displays basic application info (name, version, etc.) |
| `/actuator/metrics` | Lists available system and application metrics |
| `/actuator/prometheus` | Prometheus-compatible metrics endpoint |
| `/actuator/loggers` | Allows viewing and changing log levels at runtime |
## Testing

Run all tests:

   ```bash
   mvn test
   ```

The test suite includes:
* Unit tests for controllers, services, and repositories
* WebSocket handler tests (mocked sessions)
* JSON deserialization and request validation tests
