# Docker Microservices Setup: Complete Troubleshooting Guide

## Project Overview

**Architecture:** Spring Boot Microservices with Docker Compose  
**Components:**
- 2 MySQL Databases (product_db, order_db)
- Eureka Service Registry
- Product Service
- Order Service
- API Gateway

**Goal:** Containerize all services and enable communication via Docker networking.

---

## Initial Configuration (Before Fixes)

### Original docker-compose.yml
```yaml
version: '3.8'

services:
  mysql-product:
    image: mysql:8.0
    container_name: mysql-product
    environment:
      MYSQL_DATABASE: product_db
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3308:3306"
    networks:
      - ecommerce-net

  mysql-order:
    image: mysql:8.0
    container_name: mysql-order
    environment:
      MYSQL_DATABASE: order_db
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3307:3306"
    networks:
      - ecommerce-net

  service-registry:
    build: ./service-registry
    container_name: service-registry
    ports:
      - "8761:8761"
    networks:
      - ecommerce-net

  product-service:
    build: ./product-service
    container_name: product-service
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-product:3306/product_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-registry:8761/eureka/
    depends_on:
      - mysql-product
      - service-registry
    networks:
      - ecommerce-net

  order-service:
    build: ./order-service
    container_name: order-service
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-order:3306/order_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-registry:8761/eureka/
    depends_on:
      - mysql-order
      - service-registry
    networks:
      - ecommerce-net

  api-gateway:
    build: ./api-gateway
    container_name: api-gateway
    ports:
      - "8765:8765"
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-registry:8761/eureka/
      SPRING_CLOUD_GATEWAY_DISCOVERY_LOCATOR_ENABLED: "true"
    depends_on:
      - service-registry
      - product-service
    networks:
      - ecommerce-net

networks:
  ecommerce-net:
    driver: bridge
```

### Original application.properties Files

**service-registry/application.properties:**
```properties
spring.application.name=service-registry
server.port=8761
eureka.instance.hostname=localhost
eureka.client.fetch-registry=false
eureka.client.register-with-eureka=false
```

**product-service/application.properties:**
```properties
spring.application.name=PRODUCT-SERVICE
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/product_db
spring.datasource.username=root
spring.datasource.password=DheerajSai@16
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

**api-gateway/application.properties:**
```properties
spring.application.name=api-gateway
server.port=8765
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

---

## Challenge 1: API Gateway Cannot Connect to Eureka

### Symptom
API Gateway logs showed repeated connection failures:

```
api-gateway | Request execution error. endpoint=DefaultEndpoint{ serviceUrl='http://localhost:8761/eureka/}
api-gateway | exception=I/O error on GET request for "http://localhost:8761/eureka/apps/": 
api-gateway | Connect to http://localhost:8761 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] 
api-gateway | failed: Connection refused
```

```
api-gateway | DiscoveryClient_API-GATEWAY - was unable to send heartbeat!
api-gateway | com.netflix.discovery.shared.transport.TransportException: 
api-gateway | Cannot execute request on any known server
```

### Root Cause
Inside a Docker container, `localhost` (127.0.0.1) refers to **the container itself**, not the host machine or other containers. The api-gateway container was trying to find Eureka inside its own container, which doesn't exist.

### Docker Networking Concept
```
┌─────────────────────────────────────────────────────┐
│              Docker Network (ecommerce-net)         │
│                                                     │
│  ┌─────────────────┐      ┌─────────────────┐      │
│  │  api-gateway    │      │ service-registry│      │
│  │                 │      │                 │      │
│  │ localhost:8761  │  ✗   │    :8761        │      │
│  │ (points to self)│      │                 │      │
│  └─────────────────┘      └─────────────────┘      │
│                                                     │
│  Correct: service-registry:8761                     │
└─────────────────────────────────────────────────────┘
```

### Initial Fix Attempt
Changed environment variable in docker-compose.yml:
```yaml
api-gateway:
  environment:
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://service-registry:8761/eureka/
```

**Result:** Still failed! The service continued using `localhost:8761`.

---

## Challenge 2: Environment Variable Not Being Applied

### Symptom
Despite setting the environment variable, logs still showed:
```
endpoint=DefaultEndpoint{ serviceUrl='http://localhost:8761/eureka/}
```

### Root Cause
Spring Cloud Eureka has **inconsistent environment variable binding**. The property `eureka.client.service-url.defaultZone` should map to `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` (with underscore between SERVICE and URL), not `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`.

### Attempted Fix #1: Correct Variable Name
```yaml
environment:
  EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://service-registry:8761/eureka/
```

**Result:** Still didn't work reliably for all services.

### Final Solution: Use JAVA_TOOL_OPTIONS
`JAVA_TOOL_OPTIONS` injects JVM system properties that **always take precedence** over application.properties:

```yaml
api-gateway:
  environment:
    JAVA_TOOL_OPTIONS: "-Deureka.client.service-url.defaultZone=http://service-registry:8761/eureka/"
    SPRING_CLOUD_GATEWAY_DISCOVERY_LOCATOR_ENABLED: "true"
```

### Verification
After restart, logs showed the fix was applied:
```
api-gateway | Picked up JAVA_TOOL_OPTIONS: -Deureka.client.service-url.defaultZone=http://service-registry:8761/eureka/
```

And the connection attempt now showed the correct host:
```
Connect to http://service-registry:8761 [service-registry/172.20.0.4]
```

---

## Challenge 3: Services Starting Before Dependencies Are Ready

### Symptom
After fixing the Eureka URL, a new error appeared:
```
api-gateway | Connect to http://service-registry:8761 [service-registry/172.20.0.4] 
api-gateway | failed: Connection refused
```

The URL was correct, but connection was still refused.

### Root Cause
`depends_on` in Docker Compose only waits for the container to **start**, not for the application inside to be **ready**. Eureka takes several seconds to initialize, but api-gateway started connecting immediately.

### Timeline Problem
```
0s   - service-registry container starts
0s   - api-gateway container starts (depends_on satisfied)
2s   - api-gateway tries to connect to Eureka → FAILS
5s   - Eureka is still initializing...
10s  - Eureka finally ready
30s  - api-gateway retries → SUCCESS
```

### Temporary Behavior
The Eureka client has built-in retry logic, so after ~30 seconds it eventually connected. But this caused startup delays and error logs.

### Solution: Healthchecks with Conditions
```yaml
service-registry:
  build: ./service-registry
  container_name: service-registry
  ports:
    - "8761:8761"
  networks:
    - ecommerce-net
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 40s

api-gateway:
  depends_on:
    service-registry:
      condition: service_healthy  # Wait for healthcheck to pass
```

---

## Challenge 4: Product & Order Services Crashing (MySQL Not Ready)

### Symptom
After `docker-compose up`, only 4 containers were running:
```
NAME               STATUS
api-gateway        Up
mysql-order        Up
mysql-product      Up
service-registry   Up
```

**product-service and order-service were missing!**

### Error Logs
```
product-service | com.mysql.cj.jdbc.exceptions.CommunicationsException: 
product-service | Communications link failure
product-service | 
product-service | The last packet sent successfully to the server was 0 milliseconds ago. 
product-service | The driver has not received any packets from the server.
product-service | 
product-service | Caused by: java.net.ConnectException: Connection refused
```

```
product-service | Unable to create requested service [org.hibernate.engine.jdbc.env.spi.JdbcEnvironment] 
product-service | due to: Unable to determine Dialect without JDBC metadata
product-service | 
product-service | org.springframework.beans.factory.BeanCreationException: 
product-service | Error creating bean with name 'entityManagerFactory'
```

### Root Cause
MySQL container was **started** but not **ready** to accept connections. MySQL initialization (creating databases, setting up users) takes 20-30 seconds.

### Timeline Problem
```
0s   - mysql-product container starts
0s   - product-service container starts (depends_on satisfied)
1s   - product-service tries to connect to MySQL → FAILS
2s   - product-service crashes and exits
...
25s  - MySQL finally ready (but product-service already dead)
```

### Solution: MySQL Healthcheck
```yaml
mysql-product:
  image: mysql:8.0
  container_name: mysql-product
  environment:
    MYSQL_DATABASE: product_db
    MYSQL_ROOT_PASSWORD: root
  ports:
    - "3308:3306"
  networks:
    - ecommerce-net
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 30s

product-service:
  depends_on:
    mysql-product:
      condition: service_healthy
    service-registry:
      condition: service_healthy
```

### How mysqladmin ping Works
- `mysqladmin ping` is a lightweight command that checks if MySQL server is accepting connections
- Returns exit code 0 (success) only when MySQL is fully ready
- Docker uses this to determine container health status

---

## Challenge 5: API Gateway Returning 404 Errors

### Symptom
All services registered in Eureka successfully:
```
Application          Status
API-GATEWAY          UP (1)
ORDER-SERVICE        UP (1)
PRODUCT-SERVICE      UP (1)
```

But API calls through gateway failed:
```bash
$ curl http://localhost:8765/product-service/products
{"timestamp":"2026-02-01T18:20:21.344+00:00","path":"/product-service/products","status":404,"error":"Not Found"}
```

### Root Cause
The URL path didn't match the actual controller mapping. Spring Cloud Gateway routes to the service, but you must include the **full path** as defined in the controller.

### Controller Analysis
```java
@RestController
@RequestMapping("product")  // Base path: /product
public class ProductController {

    @GetMapping("all")      // Endpoint: /all
    public ResponseEntity<List<Product>> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("{id}")     // Endpoint: /{id}
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        return productService.getProductById(id);
    }

    @PostMapping("add")     // Endpoint: /add
    public ResponseEntity<String> addProduct(@RequestBody Product product) {
        return productService.addProduct(product);
    }
}
```

### URL Structure
```
http://localhost:8765/product-service/product/all
         │              │              │      │
         │              │              │      └── @GetMapping("all")
         │              │              └── @RequestMapping("product")
         │              └── Service name from Eureka (lowercase)
         └── API Gateway port
```

### Incorrect vs Correct URLs
| Attempted URL | Result | Correct URL |
|---------------|--------|-------------|
| `/product-service/products` | 404 | `/product-service/product/all` |
| `/product-service/api/products` | 404 | `/product-service/product/all` |
| `/products` | 404 | `/product-service/product/all` |

---

## Final Working Configuration

### Complete docker-compose.yml
```yaml
version: '3.8'

services:
  # ----------------------------
  # 1. Databases with Healthchecks
  # ----------------------------
  mysql-product:
    image: mysql:8.0
    container_name: mysql-product
    environment:
      MYSQL_DATABASE: product_db
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3308:3306"
    networks:
      - ecommerce-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  mysql-order:
    image: mysql:8.0
    container_name: mysql-order
    environment:
      MYSQL_DATABASE: order_db
      MYSQL_ROOT_PASSWORD: root
    ports:
      - "3307:3306"
    networks:
      - ecommerce-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uroot", "-proot"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  # ----------------------------
  # 2. Service Registry (Eureka)
  # ----------------------------
  service-registry:
    build: ./service-registry
    container_name: service-registry
    ports:
      - "8761:8761"
    networks:
      - ecommerce-net
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 40s

  # ----------------------------
  # 3. Microservices
  # ----------------------------
  product-service:
    build: ./product-service
    container_name: product-service
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-product:3306/product_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      JAVA_TOOL_OPTIONS: "-Deureka.client.service-url.defaultZone=http://service-registry:8761/eureka/"
    depends_on:
      mysql-product:
        condition: service_healthy
      service-registry:
        condition: service_healthy
    networks:
      - ecommerce-net

  order-service:
    build: ./order-service
    container_name: order-service
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql-order:3306/order_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      JAVA_TOOL_OPTIONS: "-Deureka.client.service-url.defaultZone=http://service-registry:8761/eureka/"
    depends_on:
      mysql-order:
        condition: service_healthy
      service-registry:
        condition: service_healthy
    networks:
      - ecommerce-net

  # ----------------------------
  # 4. API Gateway
  # ----------------------------
  api-gateway:
    build: ./api-gateway
    container_name: api-gateway
    ports:
      - "8765:8765"
    environment:
      JAVA_TOOL_OPTIONS: "-Deureka.client.service-url.defaultZone=http://service-registry:8761/eureka/"
      SPRING_CLOUD_GATEWAY_DISCOVERY_LOCATOR_ENABLED: "true"
    depends_on:
      service-registry:
        condition: service_healthy
      product-service:
        condition: service_started
      order-service:
        condition: service_started
    networks:
      - ecommerce-net

networks:
  ecommerce-net:
    driver: bridge
```

---

## Verification & Testing

### Step 1: Start All Services
```bash
docker-compose down
docker-compose up
```

### Step 2: Verify All Containers Running
```bash
docker-compose ps
```

Expected output:
```
NAME               STATUS
api-gateway        Up (healthy)
mysql-order        Up (healthy)
mysql-product      Up (healthy)
order-service      Up
product-service    Up
service-registry   Up (healthy)
```

### Step 3: Check Eureka Dashboard
Open `http://localhost:8761` in browser.

Expected registered instances:
| Application | Status |
|-------------|--------|
| API-GATEWAY | UP (1) |
| ORDER-SERVICE | UP (1) |
| PRODUCT-SERVICE | UP (1) |

### Step 4: Test API Endpoints

**Get all products (empty initially):**
```bash
curl http://localhost:8765/product-service/product/all
# Response: []
```

**Add a product:**
```bash
curl -X POST http://localhost:8765/product-service/product/add \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone","price":999.99,"stockQuantity":50}'
# Response: success
```

**Verify product added:**
```bash
curl http://localhost:8765/product-service/product/all
# Response: [{"id":1,"name":"iPhone","description":null,"category":null,"price":999.99,"stockQuantity":50}]
```

**Place an order:**
```bash
curl -X POST "http://localhost:8765/order-service/order/placeOrder?productId=1&quantity=2"
# Response: Order Placed Successfully! Order ID: 18a99cf4-75d7-4771-8dbb-c5dc5d57ec21
```

---

## Summary of Key Learnings

| Challenge | Root Cause | Solution |
|-----------|-----------|----------|
| Eureka connection refused | `localhost` in containers points to self | Use container names (e.g., `service-registry`) |
| Environment variable ignored | Spring's inconsistent property binding | Use `JAVA_TOOL_OPTIONS` with `-D` flags |
| Services start before dependencies ready | `depends_on` doesn't wait for readiness | Add healthchecks with `condition: service_healthy` |
| MySQL connection failed | MySQL not ready when app starts | MySQL healthcheck with `mysqladmin ping` |
| 404 from API Gateway | URL path mismatch | Match controller's `@RequestMapping` paths |

---

## Architecture Diagram

```
                            ┌──────────────────────┐
                            │      Client          │
                            └──────────┬───────────┘
                                       │
                                       ▼
                            ┌──────────────────────┐
                            │    API Gateway       │
                            │    localhost:8765    │
                            └──────────┬───────────┘
                                       │
                            ┌──────────▼───────────┐
                            │   Service Registry   │
                            │   (Eureka) :8761     │
                            └──────────┬───────────┘
                                       │
                 ┌─────────────────────┼─────────────────────┐
                 │                     │                     │
                 ▼                     ▼                     ▼
    ┌────────────────────┐  ┌──────────────────┐  ┌──────────────────┐
    │  Product Service   │  │  Order Service   │  │  (Future Services)│
    │       :8081        │  │      :8082       │  │                  │
    └─────────┬──────────┘  └────────┬─────────┘  └──────────────────┘
              │                      │
              ▼                      ▼
    ┌──────────────────┐    ┌──────────────────┐
    │  MySQL Product   │    │   MySQL Order    │
    │   (product_db)   │    │   (order_db)     │
    │      :3306       │    │      :3306       │
    └──────────────────┘    └──────────────────┘
```

---

## Quick Reference: API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/product-service/product/all` | Get all products |
| GET | `/product-service/product/{id}` | Get product by ID |
| GET | `/product-service/product/stock/{id}` | Get stock quantity |
| POST | `/product-service/product/add` | Add new product |
| POST | `/product-service/product/reduceStock?id=X&quantity=Y` | Reduce stock |
| POST | `/order-service/order/placeOrder?productId=X&quantity=Y` | Place order |