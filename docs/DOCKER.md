# Docker Deployment Guide

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              DOCKER NETWORK                                  │
│                         (food-delivery-network)                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐                   │
│  │   Postgres   │    │    Redis     │    │    Kafka     │                   │
│  │   (5432)     │    │   (6379)     │    │   (9092)     │                   │
│  └──────────────┘    └──────────────┘    └──────────────┘                   │
│         │                   │                   │                            │
│         └───────────────────┼───────────────────┘                            │
│                             │                                                │
│  ┌──────────────────────────┼──────────────────────────┐                    │
│  │              SPRING CLOUD INFRASTRUCTURE            │                    │
│  │  ┌──────────────┐    ┌──────────────┐              │                    │
│  │  │   Eureka     │    │   Config     │              │                    │
│  │  │   (8761)     │◄───│   Server     │              │                    │
│  │  └──────────────┘    │   (8089)     │              │                    │
│  │         ▲            └──────────────┘              │                    │
│  └─────────┼──────────────────────────────────────────┘                    │
│            │                                                                 │
│  ┌─────────┼─────────────────────────────────────────────────────────────┐  │
│  │         │              MICROSERVICES                                   │  │
│  │  ┌──────┴─────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐      │  │
│  │  │ API Gateway│  │   Auth     │  │ Restaurant │  │    Menu    │      │  │
│  │  │   (8080)   │  │  (8081)    │  │   (8082)   │  │   (8083)   │      │  │
│  │  └────────────┘  └────────────┘  └────────────┘  └────────────┘      │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐      │  │
│  │  │    Cart    │  │   Order    │  │  Payment   │  │  Delivery  │      │  │
│  │  │   (8084)   │  │   (8085)   │  │   (8086)   │  │   (8087)   │      │  │
│  │  └────────────┘  └────────────┘  └────────────┘  └────────────┘      │  │
│  │  ┌────────────┐  ┌────────────┐                                       │  │
│  │  │Notification│  │   Admin    │                                       │  │
│  │  │   (8088)   │  │   (8090)   │                                       │  │
│  │  └────────────┘  └────────────┘                                       │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                         ┌──────────────────┐
                         │   External API   │
                         │   Port: 8080     │
                         └──────────────────┘
```

## Quick Start

### Prerequisites
- Docker 24.0+
- Docker Compose 2.20+
- 8GB+ RAM recommended

### Local Development

```bash
# 1. Clone and navigate to project
cd food-delivery-microservices

# 2. Copy environment file
cp .env.docker .env

# 3. Update .env with your values (especially JWT_SECRET)

# 4. Start all services
docker-compose up -d

# 5. Check status
docker-compose ps

# 6. View logs
docker-compose logs -f api-gateway
```

### Access Points (Local Development)

| Service | URL | Purpose |
|---------|-----|---------|
| API Gateway | http://localhost:8080 | Main API entry point |
| Eureka Dashboard | http://localhost:8761 | Service registry UI |
| Config Server | http://localhost:8089 | Configuration management |
| Zipkin | http://localhost:9411 | Distributed tracing |
| pgAdmin | http://localhost:5050 | Database management |
| Redis Commander | http://localhost:8081 | Redis management |
| Kafka UI | http://localhost:9090 | Kafka management |

## File Structure

```
food-delivery-microservices/
├── docker/
│   ├── Dockerfile.service          # Generic microservice Dockerfile
│   ├── Dockerfile.service-registry # Eureka server Dockerfile
│   ├── Dockerfile.config-server    # Config server Dockerfile
│   └── init-databases.sql          # PostgreSQL initialization
├── docker-compose.yml              # Main compose file
├── docker-compose.override.yml     # Local dev overrides (auto-loaded)
├── docker-compose.render.yml       # Render.com reference
├── render.yaml                     # Render Blueprint
├── .env.docker                     # Environment template
└── docs/
    └── DOCKER.md                   # This file
```

## Commands Reference

### Basic Operations

```bash
# Start all services
docker-compose up -d

# Start specific service
docker-compose up -d api-gateway

# Stop all services
docker-compose down

# Stop and remove volumes (CAUTION: deletes data)
docker-compose down -v

# Rebuild and start
docker-compose up -d --build

# Rebuild specific service
docker-compose up -d --build user-auth-service
```

### Monitoring

```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f api-gateway

# Check service health
docker-compose ps

# Resource usage
docker stats
```

### Debugging

```bash
# Shell into container
docker-compose exec api-gateway sh

# View container details
docker inspect food-delivery-gateway

# Check network
docker network inspect food-delivery-network
```

## Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | `secure_password` |
| `JWT_SECRET` | JWT signing key (min 256 bits) | `your_256_bit_secret` |
| `JWT_EXPIRATION` | Token expiration (ms) | `86400000` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `API_GATEWAY_PORT` | Host port for API Gateway | `8080` |
| `REDIS_PASSWORD` | Redis password | (empty) |
| `ZIPKIN_URL` | Zipkin server URL | `http://zipkin:9411` |
| `LOKI_URL` | Grafana Loki URL | (empty) |

## Scaling Services

### Horizontal Scaling (Local)

```bash
# Scale a service to 3 instances
docker-compose up -d --scale order-service=3

# Note: Eureka handles load balancing automatically
```

### Production Scaling Considerations

1. **Database**: Consider separate PostgreSQL instances per service
2. **Redis**: Use Redis Cluster for high availability
3. **Kafka**: Increase partition count for parallel processing
4. **Services**: Use container orchestration (Kubernetes)

## Troubleshooting

### Common Issues

**Services not starting?**
```bash
# Check if dependencies are healthy
docker-compose ps
docker-compose logs postgres
docker-compose logs service-registry
```

**Database connection errors?**
```bash
# Verify PostgreSQL is ready
docker-compose exec postgres pg_isready -U postgres

# Check databases exist
docker-compose exec postgres psql -U postgres -c "\l"
```

**Services not registering with Eureka?**
```bash
# Check Eureka logs
docker-compose logs service-registry

# Verify network connectivity
docker-compose exec api-gateway wget -qO- http://service-registry:8761/actuator/health
```

**Out of memory?**
```bash
# Check resource usage
docker stats

# Reduce JVM memory in Dockerfile
# Change: -XX:MaxRAMPercentage=75.0
# To:     -XX:MaxRAMPercentage=50.0
```

### Health Check Endpoints

All services expose health endpoints:
```bash
# Check service health
curl http://localhost:8080/actuator/health

# Detailed health info
curl http://localhost:8080/actuator/health | jq
```

## Production Deployment

### Pre-deployment Checklist

- [ ] Strong `JWT_SECRET` generated
- [ ] Strong `DB_PASSWORD` set
- [ ] `REDIS_PASSWORD` configured
- [ ] SSL/TLS certificates configured
- [ ] Resource limits set
- [ ] Backup strategy in place
- [ ] Monitoring configured
- [ ] Log aggregation set up

### Production docker-compose

```bash
# Use only production config (no override)
docker-compose -f docker-compose.yml up -d
```

### Security Hardening

1. **Network Isolation**: Only API Gateway exposed
2. **Non-root Users**: All containers run as non-root
3. **Read-only Filesystems**: Consider adding `read_only: true`
4. **Resource Limits**: Add memory/CPU limits
5. **Secrets Management**: Use Docker secrets or external vault

## Migration to Kubernetes

The Docker setup is designed for easy K8s migration:

1. **Dockerfiles**: Already production-ready, no changes needed
2. **Environment Variables**: All config via env vars (K8s ConfigMaps/Secrets)
3. **Health Checks**: Already defined (K8s readiness/liveness probes)
4. **Service Discovery**: Replace Eureka with K8s Services
5. **Config Server**: Replace with K8s ConfigMaps or external config

### Kubernetes Manifest Example

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: food-delivery/api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 60
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 90
```

## Support

For issues or questions:
1. Check logs: `docker-compose logs -f [service-name]`
2. Verify health: `curl localhost:8080/actuator/health`
3. Review this documentation
4. Check GitHub issues
