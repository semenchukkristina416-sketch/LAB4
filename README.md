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

3. Run integration tests (`RepairOrderIT`, `OptimisticLockingIT`) from IntelliJ after starting Docker Desktop, or explicitly with Maven:

   ```shell
   mvn -Dtest=RepairOrderIT,OptimisticLockingIT test
   ```

The integration tests use Testcontainers and require access to the Docker engine. If Docker is not running, those tests fail before application logic is executed; the unit tests remain runnable independently.
