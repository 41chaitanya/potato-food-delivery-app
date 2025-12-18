-- Menu Service Seed Data
-- Restaurant IDs match restaurant-service data

-- Spice Garden (Indian) Menu Items
INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b1111111-1111-1111-1111-111111111111'::uuid, 'a1111111-1111-1111-1111-111111111111'::uuid, 
        'Butter Chicken', 'Creamy tomato-based curry with tender chicken', 299.00, 'DINNER', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b1111111-1111-1111-1111-222222222222'::uuid, 'a1111111-1111-1111-1111-111111111111'::uuid, 
        'Paneer Tikka', 'Grilled cottage cheese with spices', 249.00, 'DINNER', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b1111111-1111-1111-1111-333333333333'::uuid, 'a1111111-1111-1111-1111-111111111111'::uuid, 
        'Dal Makhani', 'Slow-cooked black lentils in creamy gravy', 199.00, 'LUNCH', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b1111111-1111-1111-1111-444444444444'::uuid, 'a1111111-1111-1111-1111-111111111111'::uuid, 
        'Naan Bread', 'Fresh baked Indian bread', 49.00, 'LUNCH', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Dragon Palace (Chinese) Menu Items
INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b2222222-2222-2222-2222-111111111111'::uuid, 'a2222222-2222-2222-2222-222222222222'::uuid, 
        'Kung Pao Chicken', 'Spicy stir-fried chicken with peanuts', 279.00, 'DINNER', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b2222222-2222-2222-2222-222222222222'::uuid, 'a2222222-2222-2222-2222-222222222222'::uuid, 
        'Veg Fried Rice', 'Wok-tossed rice with vegetables', 179.00, 'LUNCH', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b2222222-2222-2222-2222-333333333333'::uuid, 'a2222222-2222-2222-2222-222222222222'::uuid, 
        'Spring Rolls', 'Crispy vegetable rolls', 149.00, 'SNACKS', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Pizza Italia Menu Items
INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b3333333-3333-3333-3333-111111111111'::uuid, 'a3333333-3333-3333-3333-333333333333'::uuid, 
        'Margherita Pizza', 'Classic tomato and mozzarella pizza', 349.00, 'DINNER', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b3333333-3333-3333-3333-222222222222'::uuid, 'a3333333-3333-3333-3333-333333333333'::uuid, 
        'Pasta Alfredo', 'Creamy white sauce pasta', 299.00, 'LUNCH', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Burger Barn Menu Items
INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b4444444-4444-4444-4444-111111111111'::uuid, 'a4444444-4444-4444-4444-444444444444'::uuid, 
        'Classic Burger', 'Juicy beef patty with fresh veggies', 199.00, 'LUNCH', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b4444444-4444-4444-4444-222222222222'::uuid, 'a4444444-4444-4444-4444-444444444444'::uuid, 
        'French Fries', 'Crispy golden fries', 99.00, 'SNACKS', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Chaat Corner Menu Items
INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b5555555-5555-5555-5555-111111111111'::uuid, 'a5555555-5555-5555-5555-555555555555'::uuid, 
        'Pani Puri', 'Crispy puris with spicy water', 79.00, 'SNACKS', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO menu_items (id, restaurant_id, name, description, price, meal_type, occasion_type, status, available, created_at, updated_at)
VALUES ('b5555555-5555-5555-5555-222222222222'::uuid, 'a5555555-5555-5555-5555-555555555555'::uuid, 
        'Samosa', 'Crispy potato-filled pastry', 49.00, 'SNACKS', 'REGULAR', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
