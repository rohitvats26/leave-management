# Leave Management System — Microservices
**Spring Boot 4.0.6 · Java 21 · Kafka · Resilience4j · ELK Stack · Docker**

## Architecture
| Service          | Port | Notes                                                                |
|------------------|------|----------------------------------------------------------------------|
| Eureka Server    | 8761 | Service discovery                                                    |
| API Gateway      | 8080 | JWT filter, routing, load balancer                                   |
| Auth Service     | 8081 | Login, JWT generation                                                |
| Employee Service | 8082 | Profiles, leave balances,<br/> Team view, delegates to Leave Service |
| Leave Service    | 8083 | Apply/approve/reject, Kafka producer, CB                             |
| Notification Svc | 8085 | Kafka consumer, notification logs                                    |
| Kafka (KRaft)    | 9092 | Zookeeper-free message broker                                        |
| Kafka UI         | 8090 | Topic/message browser                                                |
| Elasticsearch    | 9200 | Log index store                                                      |
| Logstash         | 5044 | Log ingestion pipeline (TCP JSON)                                    |
| Kibana           | 5601 | Log dashboards                                                       |

## Quick Start
```bash
docker-compose up --build
```
All services start in dependency order (~3 min first run, ~90s warm rebuild).

## Default Credentials
| Username       | Password       | Role           |
|----------------|----------------|----------------|
| john.smith     | Password@123   | ROLE_EMPLOYEE  |
| priya.sharma   | Password@123   | ROLE_EMPLOYEE  |
| rahul.verma    | Password@123   | ROLE_MANAGER   |
| anita.gupta    | Password@123   | ROLE_MANAGER   |

## Key URLs
| URL                                             | Purpose                  |
|-------------------------------------------------|--------------------------|
| http://localhost:8080/auth/login                | Login                    |
| http://localhost:8761                           | Eureka dashboard         |
| http://localhost:8090                           | Kafka UI                 |
| http://localhost:5601                           | Kibana (ELK)             |
| http://localhost:9200/_cluster/health           | Elasticsearch health     |
| http://localhost:8083/actuator/circuitbreakers  | Circuit breaker states   |

## Kibana Setup (first run)
1. Open http://localhost:5601
2. Go to Stack Management → Index Patterns
3. Create pattern: `lms-*`
4. Set `@timestamp` as time field
5. Go to Discover → select `lms-*` to view all service logs

## Kafka Topics
| Topic           | Producer      | Consumer             |
|-----------------|---------------|----------------------|
| leave.applied   | leave-service | notification-service |
| leave.approved  | leave-service | notification-service |
| leave.rejected  | leave-service | notification-service |

## Circuit Breaker States
Check live states:
```
GET http://localhost:8083/actuator/circuitbreakers
GET http://localhost:8083/actuator/circuitbreakerevents
```

## Environment Variables
| Variable                             | Default Value                     |
|--------------------------------------|-----------------------------------|
| JWT_SECRET                           | lms-super-secret-key-...          |
| SPRING_KAFKA_BOOTSTRAP_SERVERS       | kafka:9092                        |
| EUREKA_CLIENT_SERVICEURL_DEFAULTZONE | http://eureka-server:8761/eureka/ |
| LOGSTASH_HOST                        | logstash                          |
| LOGSTASH_PORT                        | 5044                              |


## 1. REST API Guidelines Implementation

### Endpoint Conventions
- **Collection endpoints:** `GET /resource`, `POST /resource` (create)
- **Resource endpoints:** `GET /resource/{id}`, `PUT /resource/{id}` (update), `DELETE /resource/{id}`
- **Query filters:** Pagination (`page`, `size`), status filtering, sorting
- **Backward-compatible aliases:**
    - `/leaves` + `/leaves/apply` → POST create leave
    - `/leaves/me` + `/leaves/my` → GET employee leaves
    - `/notifications/me` + `/notifications/my` → GET my notifications

### HTTP Status Codes Standardized Across All Services

| Status                        | Usage                                 | Services                                    |
|-------------------------------|---------------------------------------|---------------------------------------------|
| **201 Created**               | POST creates resource                 | leave-service, employee-service             |
| **200 OK**                    | Successful read/update                | All services                                |
| **204 No Content**            | DELETE or successful empty success    | employee-service (balance deduct)           |
| **400 Bad Request**           | Validation/format errors              | All services                                |
| **401 Unauthorized**          | Missing/invalid auth                  | auth-service                                |
| **403 Forbidden**             | Authenticated but not allowed         | All services                                |
| **404 Not Found**             | Resource missing                      | All services                                |
| **409 Conflict**              | Duplicate/state conflict              | employee-service (duplicate email/username) |
| **422**                       | Business rule failure                 | Reserved for future use                     |
| **503 Service Unavailable**   | Circuit-breaker or downstream failure | leave-service                               |
| **500 Internal Server Error** | Unexpected failure (safe message)     | All services                                |

---

## 2. Standardized Error Response Schema

All services return this consistent JSON payload:

```json
{
  "timestamp": "2026-06-02T10:15:30Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "path": "/api/v1/leaves",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```
