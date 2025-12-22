-- ============================================================================
-- COMPLETE DATA SEED SCRIPT - Run in psql
-- Password: 1122334455
-- ============================================================================

-- ==================== USER_AUTH_DB ====================
\c user_auth_db

INSERT INTO users (id, name, email, password, role, phone, address, active, created_at, updated_at)
VALUES 
('11111111-1111-1111-1111-111111111111', 'Admin User', 'admin@foodapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 'ADMIN', '9876543210', 'Admin Office, Tech Park', true, NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', 'Rahul Sharma', 'rahul@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 'USER', '9876543211', '123 MG Road, Bangalore', true, NOW(), NOW()),
('33333333-3333-3333-3333-333333333333', 'Priya Patel', 'priya@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 'USER', '9876543212', '456 Koramangala, Bangalore', true, NOW(), NOW()),
('55555555-5555-5555-5555-555555555555', 'Raju Rider', 'raju.rider@foodapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 'RIDER', '9876543214', 'HSR Layout, Bangalore', true, NOW(), NOW());

-- ==================== RESTAURANT_DB ====================
\c restaurant_db

INSERT INTO restaurants (id, name, address, phone, cuisine_type, status, active, created_at, updated_at)
VALUES 
('a1111111-1111-1111-1111-111111111111', 'Spice Garden', 'MG Road, Bangalore', '9800000001', 'INDIAN', 'ACTIVE', true, NOW(), NOW()),
('a2222222-2222-2222-2222-222222222222', 'Dragon Palace', 'Indiranagar, Bangalore', '9800000003', 'CHINESE', 'ACTIVE', true, NOW(), NOW()),
('a3333333-3333-3333-3333-333333333333', 'Pizza Italia', 'HSR Layout, Bangalore', '9800000004', 'ITALIAN', 'ACTIVE', true, NOW(), NOW()),
('a4444444-4444-4444-4444-444444444444', 'Burger Barn', 'Whitefield, Bangalore', '9800000005', 'FAST_FOOD', 'ACTIVE', true, NOW(), NOW());

-- ==================== MENU_DB ====================
\c menu_db

INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES 
('b1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', 'Butter Chicken', 'Creamy tomato-based curry with tender chicken', 299.00, 'DINNER', 'REGULAR', 'ACTIVE', true, NOW(), NOW()),
('b1111111-1111-1111-1111-222222222222', 'a1111111-1111-1111-1111-111111111111', 'Paneer Tikka', 'Grilled cottage cheese with spices', 249.00, 'DINNER', 'REGULAR', 'ACTIVE', true, NOW(), NOW()),
('b2222222-2222-2222-2222-111111111111', 'a2222222-2222-2222-2222-222222222222', 'Kung Pao Chicken', 'Spicy stir-fried chicken with peanuts', 279.00, 'DINNER', 'REGULAR', 'ACTIVE', true, NOW(), NOW()),
('b3333333-3333-3333-3333-111111111111', 'a3333333-3333-3333-3333-333333333333', 'Margherita Pizza', 'Classic tomato and mozzarella pizza', 349.00, 'DINNER', 'REGULAR', 'ACTIVE', true, NOW(), NOW()),
('b4444444-4444-4444-4444-111111111111', 'a4444444-4444-4444-4444-444444444444', 'Classic Burger', 'Juicy beef patty with fresh veggies', 199.00, 'LUNCH', 'REGULAR', 'ACTIVE', true, NOW(), NOW());

-- ==================== ORDER_DB ====================
\c order_db

INSERT INTO orders_tb (id, user_id, customer_name, restaurant_name, total_amount, status, created_at, updated_at)
VALUES 
('01111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'Rahul Sharma', 'Spice Garden', 547.00, 'DELIVERED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
('02222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'Priya Patel', 'Dragon Palace', 458.00, 'DELIVERED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day');

-- ==================== PAYMENT_DB ====================
\c payment_db

INSERT INTO payments_tb (id, order_id, amount, status, payment_time)
VALUES 
('a1111111-1111-1111-1111-111111111111', '01111111-1111-1111-1111-111111111111', 547.00, 'SUCCESS', NOW() - INTERVAL '2 days'),
('a2222222-2222-2222-2222-222222222222', '02222222-2222-2222-2222-222222222222', 458.00, 'SUCCESS', NOW() - INTERVAL '1 day');

-- Done!
SELECT 'Data seeded successfully!' as status;
