# Laboratory Work #2: Clean Architecture and Unit Testing in Java

A console application for a car repair workshop management, implemented following Clean Architecture principles, utilizing a domain model, repositories, pricing policies, and automated tests.

## Technologies
- **Programming Language**: Java 17 (LTS)
- **Build & Dependency Management**: Apache Maven
- **Code Generation Library**: Lombok (v1.18.36)
- **Testing**: JUnit 5, Mockito

---

## Project Structure
The project is divided into distinct architectural packages:
- `domain` — domain model (`RepairOrder`, `Job`, `Mechanic`, `Car`, `Client` entities, business exceptions, and business rules).
- `service` — application services (`RepairOrderService`) for scenario coordination.
- `repository` — repository interfaces and their In-Memory implementation (`RepairOrderRepository`).
- `pricing` — pricing components and rules (`PriceCalculator`, `PricingRule`, `DiscountRule`, `MinimumPriceRule`).
- `test` — unit and integration tests (JUnit 5 + Mockito).

---

## Installation and Execution Guide

### 1. Requirements
- Installed **Java 17** (or newer).
- Installed **Maven** (or using the built-in one within your IntelliJ IDEA IDE).

### 2. Cloning or Opening the Project
1. Open the **IntelliJ IDEA** development environment.
2. Select **File ➔ Open** and specify the path to the project folder (`C:\java\lab1`).
3. Ensure in the project settings (*File ➔ Project Structure*) that both **Project SDK** and **Language level** are set to **17**.

### 3. Building and Running Tests
To download dependencies and run all automated tests:
1. On the right panel, click the two circular arrows **`Reload All Maven Projects`**.