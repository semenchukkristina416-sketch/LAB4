CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS mechanics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialization VARCHAR(100) NOT NULL
    );

CREATE TABLE IF NOT EXISTS cars (
    id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    license_plate VARCHAR(50) UNIQUE NOT NULL,
    client_id BIGINT REFERENCES clients(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS repair_orders (
    id BIGSERIAL PRIMARY KEY,
    car_id BIGINT NOT NULL,
    mechanic_id BIGINT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_price NUMERIC(10, 2),
    version BIGINT DEFAULT 0 NOT NULL,
    CONSTRAINT fk_car FOREIGN KEY (car_id) REFERENCES cars(id),
    CONSTRAINT fk_mechanic FOREIGN KEY (mechanic_id) REFERENCES mechanics(id)
    );

CREATE TABLE IF NOT EXISTS jobs (
                                    id BIGSERIAL PRIMARY KEY,
                                    repair_order_id BIGINT REFERENCES repair_orders(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    labor_cost NUMERIC(10, 2),
    parts_cost NUMERIC(10, 2),
    base_price NUMERIC(10, 2) NOT NULL,
    commercial_price NUMERIC(10, 2) NOT NULL,
    required_specialization VARCHAR(100) NOT NULL,
    status VARCHAR(50)
    );