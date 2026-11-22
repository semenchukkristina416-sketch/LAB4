# Laboratory Work #2: Clean Architecture and Unit Testing in Java

A console application for a car repair workshop management, implemented following Clean Architecture principles, utilizing a domain model, repositories, pricing policies, and automated tests.

## Technologies
- **Programming Language**: Java 25
- **Build & Dependency Management**: Apache Maven
- **Code Generation Library**: Lombok (v1.18.48)
- **Testing**: JUnit 5, Mockito

---

## Project Structure
The project is divided into distinct architectural packages:
- `domain` — domain entities and value objects (`RepairOrder`, `Job`, `Mechanic`, `Car`, `Client`, `TimeInterval`).
- `domain.enumeration` — domain status, priority, and specialization enumerations.
- `exception` — application and domain exceptions (`DomainException`).
- `service` — application services (`RepairOrderService`, `ClientService`, `CarService`, `MechanicService`) for scenario coordination and persistence access.
- `repository` — Spring Data repository interfaces for domain entities.
- `pricing` — pricing components and rules (`PriceCalculator`, `PricingRule`, `DiscountRule`, `MinimumPriceRule`).
- `test` — unit and integration tests (JUnit 5 + Mockito).

---

## Installation and Execution Guide

### 1. Requirements
- Installed **JDK 25** (or newer).
- Installed **Maven** (or using the built-in one within your IntelliJ IDEA IDE).
- **Docker Desktop** running for integration tests (Testcontainers starts PostgreSQL automatically).

### 2. Cloning or Opening the Project
1. Open the **IntelliJ IDEA** development environment.
2. Select **File ➔ Open** and specify the path to the project folder (`C:\java\lab1`).
3. Ensure in the project settings (*File ➔ Project Structure*) that the **Project SDK** is JDK 26 (or newer) and the **Language level** is set to 25.

### 3. Building and Running Tests
1. On the right panel, click the two circular arrows **`Reload All Maven Projects`**.
2. Run unit tests without Docker:

   ```shell
   mvn test -DskipITs
   ```

3. Run integration and E2E tests (`*IT`, `*E2E`) after starting Docker Desktop:

   ```shell
   mvn verify
   ```

The integration tests use Testcontainers and require access to the Docker engine. If Docker is not running, those tests fail before application logic is executed; the unit tests remain runnable independently.

### 4. REST API

The application exposes the following resources under `/api`:

- `clients`, `cars`, and `mechanics`: create and get by identifier;
- `orders`: create, get, filter by status, paginate, and sort;
- order actions: `approve`, `start`, `complete`, and `cancel`;
- order jobs: add one job, add a batch, start a job, and mark a job completed.

State transitions are separate `POST` actions. There is no generic status update endpoint. Requests use DTOs with Bean Validation, while responses never expose JPA entities. Errors use RFC 9457 `ProblemDetail`; validation errors include a `fieldErrors` object. Business-rule conflicts and duplicate license plates return `409 Conflict`.

An IntelliJ HTTP request collection is available in [`car-service.http`](C:/java/lab1/car-service.http).

### 5. Test levels

- Unit tests: domain, pricing, services, DTO/controller validation, and parameterized invalid-input cases.
- Integration tests: PostgreSQL Testcontainers with the same Flyway migrations as the application, including a full MockMvc request.
- E2E tests: `CarServiceE2E` uses `@SpringBootTest(webEnvironment = RANDOM_PORT)` and `RestTestClient`; it communicates only through HTTP.

Test data is generated per scenario and database-backed tests clean their data between cases. A transaction opened by an E2E test would not roll back changes made by the separately running HTTP server, so E2E isolation is handled through independent data and cleanup rather than a test transaction.
