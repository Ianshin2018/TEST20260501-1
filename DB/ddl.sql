CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    registration_time TIMESTAMP NOT NULL,
    last_login_time TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS books (
    isbn VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    author VARCHAR(100) NOT NULL,
    introduction VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS inventory (
    inventory_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL,
    store_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_inventory_book FOREIGN KEY (isbn) REFERENCES books (isbn)
);

CREATE TABLE IF NOT EXISTS borrowing_record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    inventory_id BIGINT NOT NULL,
    borrowing_time TIMESTAMP NOT NULL,
    return_time TIMESTAMP NULL,
    CONSTRAINT fk_borrow_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_borrow_inventory FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id)
);

CREATE ALIAS IF NOT EXISTS REGISTER_USER FOR "com.example.library.db.H2StoredProcedures.registerUser";
CREATE ALIAS IF NOT EXISTS AUTHENTICATE_USER FOR "com.example.library.db.H2StoredProcedures.authenticateUser";
