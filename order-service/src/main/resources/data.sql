-- Order Service Seed Data
-- Sample orders for testing

INSERT INTO orders_tb (id, user_id, customer_name, restaurant_name, total_amount, status, created_at, updated_at)
VALUES ('01111111-1111-1111-1111-111111111111'::uuid, '22222222-2222-2222-2222-222222222222'::uuid, 
        'Rahul Sharma', 'Spice Garden', 547.00, 'DELIVERED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO orders_tb (id, user_id, customer_name, restaurant_name, total_amount, status, created_at, updated_at)
VALUES ('02222222-2222-2222-2222-222222222222'::uuid, '33333333-3333-3333-3333-333333333333'::uuid, 
        'Priya Patel', 'Dragon Palace', 458.00, 'DELIVERED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;

INSERT INTO orders_tb (id, user_id, customer_name, restaurant_name, total_amount, status, created_at, updated_at)
VALUES ('03333333-3333-3333-3333-333333333333'::uuid, '44444444-4444-4444-4444-444444444444'::uuid, 
        'Amit Kumar', 'Pizza Italia', 648.00, 'CONFIRMED', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO orders_tb (id, user_id, customer_name, restaurant_name, total_amount, status, created_at, updated_at)
VALUES ('04444444-4444-4444-4444-444444444444'::uuid, '22222222-2222-2222-2222-222222222222'::uuid, 
        'Rahul Sharma', 'Burger Barn', 298.00, 'PAID', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
