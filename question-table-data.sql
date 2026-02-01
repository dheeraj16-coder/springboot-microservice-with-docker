-- Database for the Product Service
CREATE DATABASE product_db;

-- Database for the Order Service
CREATE DATABASE order_db;

USE product_db;

CREATE TABLE product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50),
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL
);

-- Seed Data
INSERT INTO product (name, description, category, price, stock_quantity) VALUES
('MacBook Pro', 'M2 Chip, 16GB RAM', 'Electronics', 1299.99, 50),
('iPhone 15', '128GB, Black', 'Electronics', 799.99, 100),
('Nike Air Max', 'Size 10, White', 'Footwear', 120.00, 200),
('Java Programming Head First', '3rd Edition', 'Books', 45.50, 75);

USE order_db;

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) UNIQUE NOT NULL,
    sku_code VARCHAR(255),
    price DECIMAL(10, 2),
    quantity INT,
    total_amount DECIMAL(10, 2),
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP
);