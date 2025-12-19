# ============================================================================
# MAKEFILE - Food Delivery Microservices
# ============================================================================
# Commands for building and managing the project
# ============================================================================

.PHONY: build clean test

# Build all services
build:
	@echo "Building all services..."
	cd service-registry && ./mvnw clean package -DskipTests
	cd config-server && ./mvnw clean package -DskipTests
	cd api-gateway && ./mvnw clean package -DskipTests
	cd user-auth-service && ./mvnw clean package -DskipTests
	cd restaurant-service && ./mvnw clean package -DskipTests
	cd menu-service && ./mvnw clean package -DskipTests
	cd cart-service && ./mvnw clean package -DskipTests
	cd order-service && ./mvnw clean package -DskipTests
	cd payment-service && ./mvnw clean package -DskipTests
	cd delivery-service && ./mvnw clean package -DskipTests
	cd notification-service && ./mvnw clean package -DskipTests
	cd admin-service && ./mvnw clean package -DskipTests
	@echo "Build complete!"

# Clean all services
clean:
	@echo "Cleaning all services..."
	cd service-registry && ./mvnw clean
	cd config-server && ./mvnw clean
	cd api-gateway && ./mvnw clean
	cd user-auth-service && ./mvnw clean
	cd restaurant-service && ./mvnw clean
	cd menu-service && ./mvnw clean
	cd cart-service && ./mvnw clean
	cd order-service && ./mvnw clean
	cd payment-service && ./mvnw clean
	cd delivery-service && ./mvnw clean
	cd notification-service && ./mvnw clean
	cd admin-service && ./mvnw clean
	@echo "Clean complete!"

# Run tests
test:
	@echo "Running tests..."
	cd user-auth-service && ./mvnw test
	cd restaurant-service && ./mvnw test
	cd menu-service && ./mvnw test
	cd cart-service && ./mvnw test
	cd order-service && ./mvnw test
	cd payment-service && ./mvnw test
	cd delivery-service && ./mvnw test
	cd notification-service && ./mvnw test
	cd admin-service && ./mvnw test
	@echo "Tests complete!"
