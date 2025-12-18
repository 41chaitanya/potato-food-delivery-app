# Observability Stack - Centralized Logging

Production-grade centralized logging for the Food Delivery microservices platform using **Loki + Promtail + Grafana**.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         MICROSERVICES                                    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │ cart-svc │ │ order-svc│ │ menu-svc │ │ user-svc │ │ payment  │ ...  │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘      │
│       │            │            │            │            │             │
│       └────────────┴────────────┴────────────┴────────────┘             │
│                              │ STDOUT (JSON)                            │
└──────────────────────────────┼──────────────────────────────────────────┘
                               ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                         OBSERVABILITY STACK                              │
│                                                                          │
│  ┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐  │
│  │    PROMTAIL     │─────▶│      LOKI       │◀─────│    GRAFANA      │  │
│  │  (Log Shipper)  │      │  (Log Storage)  │      │ (Visualization) │  │
│  │   :9080         │      │   :3100         │      │   :3000         │  │
│  └─────────────────┘      └─────────────────┘      └─────────────────┘  │
│         │                                                                │
│         │ Scrapes Docker container logs via docker.sock                  │
└─────────┼────────────────────────────────────────────────────────────────┘
          ▼
    /var/run/docker.sock
```

## Quick Start

### 1. Start the Observability Stack

```bash
cd observability
docker-compose up -d
```

### 2. Verify Services

| Service   | URL                     | Credentials      |
|-----------|-------------------------|------------------|
| Grafana   | http://localhost:3000   | admin / admin123 |
| Loki      | http://localhost:3100   | -                |
| Promtail  | http://localhost:9080   | -                |

### 3. Check Loki Health

```bash
curl http://localhost:3100/ready
# Expected: ready
```

### 4. View Logs in Grafana

1. Open http://localhost:3000
2. Login with `admin` / `admin123`
3. Go to **Explore** → Select **Loki** datasource
4. Run query: `{job="docker-containers"}`

## Log Format

Each log entry contains:

```json
{
  "timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "service": "cart-service",
  "class": "c.m.cart_service.controller.CartController",
  "thread": "http-nio-8080-exec-1",
  "message": "Adding item to cart for user: abc-123"
}
```

## Useful LogQL Queries

### All logs from a specific service
```logql
{service="cart-service"}
```

### Error logs only
```logql
{job="docker-containers"} |= "ERROR"
```

### Logs containing specific text
```logql
{job="docker-containers"} |= "user"
```

### Logs from multiple services
```logql
{service=~"cart-service|order-service"}
```

### Error rate over time
```logql
sum(rate({job="docker-containers"} |= "ERROR" [5m])) by (service)
```

## Folder Structure

```
observability/
├── docker-compose.yml          # Stack orchestration
├── loki-config.yml             # Loki storage/retention config
├── promtail-config.yml         # Log scraping config
├── provisioning/
│   ├── datasources/
│   │   └── datasources.yml     # Auto-configure Loki in Grafana
│   └── dashboards/
│       ├── dashboards.yml      # Dashboard provisioning
│       └── json/
│           └── microservices-logs.json  # Pre-built dashboard
└── README.md
```

## Spring Boot Configuration

Each microservice needs `logback-spring.xml` in `src/main/resources/`:

```xml
<!-- See cart-service/src/main/resources/logback-spring.xml for full example -->
```

Run services with `docker` profile for JSON logging:
```bash
java -jar app.jar --spring.profiles.active=docker
```

## Troubleshooting

### Logs not appearing in Loki?

1. Check Promtail is running:
   ```bash
   docker logs promtail
   ```

2. Verify docker.sock is mounted:
   ```bash
   docker exec promtail ls -la /var/run/docker.sock
   ```

3. Check Loki is receiving logs:
   ```bash
   curl http://localhost:3100/loki/api/v1/labels
   ```

### Grafana can't connect to Loki?

Ensure both are on the same Docker network (`observability`).

## Production Considerations

- **Retention**: Currently set to 31 days. Adjust in `loki-config.yml`
- **Storage**: Using local filesystem. For production, use S3/GCS
- **Scaling**: This is single-node. For HA, deploy Loki in microservices mode
- **Security**: Add authentication to Grafana and Loki for production
