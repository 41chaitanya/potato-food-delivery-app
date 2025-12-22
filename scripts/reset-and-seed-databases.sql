-- ============================================================================
-- COMPLETE DATABASE RESET AND SEED SCRIPT
-- Run this in psql as postgres user
-- Password: 1122334455
-- ============================================================================

-- Step 1: Terminate all connections
SELECT pg_terminate_backend(pid) FROM pg_stat_activity 
WHERE datname IN ('admin_db', 'cart_db', 'delivery_db', 'menu_db', 'notification_db', 'order_db', 'payment_db', 'restaurant_db', 'user_auth_db') 
AND pid <> pg_backend_pid();

-- Step 2: Drop all databases
DROP DATABASE IF EXISTS admin_db;
DROP DATABASE IF EXISTS cart_db;
DROP DATABASE IF EXISTS delivery_db;
DROP DATABASE IF EXISTS menu_db;
DROP DATABASE IF EXISTS notification_db;
DROP DATABASE IF EXISTS order_db;
DROP DATABASE IF EXISTS payment_db;
DROP DATABASE IF EXISTS restaurant_db;
DROP DATABASE IF EXISTS user_auth_db;

-- Step 3: Recreate all databases
CREATE DATABASE admin_db;
CREATE DATABASE cart_db;
CREATE DATABASE delivery_db;
CREATE DATABASE menu_db;
CREATE DATABASE notification_db;
CREATE DATABASE order_db;
CREATE DATABASE payment_db;
CREATE DATABASE restaurant_db;
CREATE DATABASE user_auth_db;

-- ============================================================================
-- NOTE: Data seeding will happen automatically when services start
-- because Spring Boot runs data.sql files on startup with ddl-auto: update
-- ============================================================================
