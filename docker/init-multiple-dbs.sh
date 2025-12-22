#!/bin/bash
set -e

# Create multiple databases
for db in user_auth_db restaurant_db menu_db cart_db order_db payment_db delivery_db admin_db notification_db; do
    echo "Creating database: $db"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE DATABASE $db;
EOSQL
done

echo "All databases created successfully!"
