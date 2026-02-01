# 🛒 Microservices E-Commerce Platform

A fully containerized microservices architecture built with Spring Boot, Docker, Eureka Service Discovery, and API Gateway.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.x-brightgreen)
![Docker](https://img.shields.io/badge/Docker-Compose-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

## 🏗️ Architecture

```
                            ┌──────────────────────┐
                            │       Client         │
                            └──────────┬───────────┘
                                       │
                                       ▼
                            ┌──────────────────────┐
                            │     API Gateway      │
                            │       :8765          │
                            └──────────┬───────────┘
                                       │
                            ┌──────────▼───────────┐
                            │   Service Registry   │
                            │   (Eureka) :8761     │
                            └──────────┬───────────┘
                                       │
                 ┌─────────────────────┴─────────────────────┐
                 │                                           │
                 ▼                                           ▼
    ┌────────────────────┐                      ┌────────────────────┐
    │  Product Service   │                      │   Order Service    │
    │       :8081        │◄────────────────────►│       :8082        │
    └─────────┬──────────┘                      └──────────┬─────────┘
              │                                            │
              ▼                                            ▼
    ┌──────────────────┐                        ┌──────────────────┐
    │  MySQL Product   │                        │   MySQL Order    │
    │   (product_db)   │                        │    (order_db)    │
    └──────────────────┘                        └──────────────────┘
```

## ✨ Features

- **Service Discovery** - Automatic service registration with Netflix Eureka
- **API Gateway** - Single entry point for all client requests
- **Load Balancing** - Built-in client-side load balancing
- **Database per Service** - Each microservice has its own MySQL database
- **Health Checks** - Automated health monitoring for all services
- **Containerized** - Fully dockerized for easy deployment

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Backend | Spring Boot 3.1.x |
| Language | Java 17 |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Database | MySQL 8.0 |
| Containerization | Docker & Docker Compose |
| Build Tool | Maven |

## 📋 Prerequisites

Only one requirement:

- **Docker Desktop** - [Download here](https://www.docker.com/products/docker-desktop/)

That's it! No need to install Java, Maven, or MySQL locally.

## 🚀 Quick Start

### 1. Clone the repository
```bash
git clone https://github.com/yourusername/microservices-ecommerce.git
cd microservices-ecommerce
```

### 2. Start all services
```bash
docker-compose up --build
```

### 3. Wait for services to be ready (~60-90 seconds)
You'll see all services register with Eureka in the logs.

### 4. Verify everything is running
Open [http://localhost:8761](http://localhost:8761) to see the Eureka dashboard.

You should see:
| Application | Status |
|-------------|--------|
| API-GATEWAY | UP |
| PRODUCT-SERVICE | UP |
| ORDER-SERVICE | UP |

## 🌐 Service URLs

| Service | URL | Description |
|---------|-----|-------------|
| Eureka Dashboard | http://localhost:8761 | Service registry UI |
| API Gateway | http://localhost:8765 | Main entry point |
| Product Service | http://localhost:8081 | Direct access (dev only) |
| Order Service | http://localhost:8082 | Direct access (dev only) |

## 📡 API Endpoints

All requests go through the **API Gateway** at `http://localhost:8765`

### Product Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/product-service/product/all` | Get all products |
| GET | `/product-service/product/{id}` | Get product by ID |
| GET | `/product-service/product/stock/{id}` | Get stock quantity |
| POST | `/product-service/product/add` | Add new product |
| POST | `/product-service/product/reduceStock` | Reduce stock |

### Order Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/order-service/order/placeOrder` | Place a new order |

## 🧪 Testing the APIs

### Add a Product
```bash
curl -X POST http://localhost:8765/product-service/product/add \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15",
    "description": "Latest Apple smartphone",
    "category": "Electronics",
    "price": 999.99,
    "stockQuantity": 50
  }'
```

### Get All Products
```bash
curl http://localhost:8765/product-service/product/all
```

### Get Product by ID
```bash
curl http://localhost:8765/product-service/product/1
```

### Check Stock
```bash
curl http://localhost:8765/product-service/product/stock/1
```

### Place an Order
```bash
curl -X POST "http://localhost:8765/order-service/order/placeOrder?productId=1&quantity=2"
```

## 📁 Project Structure

```
microservices-ecommerce/
├── docker-compose.yml          # Container orchestration
├── README.md
│
├── service-registry/           # Eureka Server
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
├── api-gateway/                # Spring Cloud Gateway
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
├── product-service/            # Product microservice
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
└── order-service/              # Order microservice
    ├── Dockerfile
    ├── pom.xml
    └── src/
```

## 🔧 Common Commands

### Start services (detached mode)
```bash
docker-compose up -d --build
```

### View logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f product-service
```

### Stop all services
```bash
docker-compose down
```

### Stop and remove volumes (reset databases)
```bash
docker-compose down -v
```

### Rebuild a specific service
```bash
docker-compose up -d --build product-service
```

### Check service status
```bash
docker-compose ps
```

## 🐛 Troubleshooting

### Services not appearing in Eureka?
Wait 30-60 seconds. Services need time to register. Check logs:
```bash
docker-compose logs -f service-registry
```

### Database connection errors?
MySQL takes ~30 seconds to initialize. The healthchecks handle this automatically, but if issues persist:
```bash
docker-compose down -v
docker-compose up --build
```

### Port already in use?
Check if ports 8761, 8765, 8081, 8082, 3307, or 3308 are already in use:
```bash
# Mac/Linux
lsof -i :8761

# Windows
netstat -ano | findstr :8761
```

### Need to start fresh?
```bash
docker-compose down -v --rmi all
docker-compose up --build
```

## 🔄 How It Works

1. **Service Registry (Eureka)** starts first and waits for health check
2. **MySQL databases** start and wait until ready to accept connections
3. **Product & Order Services** start after their dependencies are healthy
4. **API Gateway** starts after Eureka is healthy
5. All services **register themselves** with Eureka
6. API Gateway **discovers services** via Eureka and routes requests

## 📝 Configuration

Key environment variables in `docker-compose.yml`:

| Variable | Purpose |
|----------|---------|
| `SPRING_DATASOURCE_URL` | Database connection string |
| `JAVA_TOOL_OPTIONS` | JVM args for Eureka URL |
| `SPRING_CLOUD_GATEWAY_DISCOVERY_LOCATOR_ENABLED` | Enable gateway routing |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot & Spring Cloud teams
- Docker community
- Netflix OSS (Eureka)

---

⭐ **Star this repo if you found it helpful!**