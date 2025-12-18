-- Cart Service Seed Data
-- Sample active carts for users

-- Rahul's active cart at Spice Garden
INSERT INTO carts (id, user_id, restaurant_id, total_amount, active, created_at, updated_at)
VALUES ('c1111111-1111-1111-1111-111111111111'::uuid, '22222222-2222-2222-2222-222222222222'::uuid, 
        'a1111111-1111-1111-1111-111111111111'::uuid, 547.00, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Cart items for Rahul
INSERT INTO cart_items (id, cart_id, menu_item_id, quantity, price_per_item, total_price)
VALUES ('c1111111-1111-1111-1111-111111111112'::uuid, 'c1111111-1111-1111-1111-111111111111'::uuid, 
        'b1111111-1111-1111-1111-111111111111'::uuid, 1, 299.00, 299.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO cart_items (id, cart_id, menu_item_id, quantity, price_per_item, total_price)
VALUES ('c1111111-1111-1111-1111-222222222222'::uuid, 'c1111111-1111-1111-1111-111111111111'::uuid, 
        'b1111111-1111-1111-1111-222222222222'::uuid, 1, 249.00, 249.00)
ON CONFLICT (id) DO NOTHING;

-- Priya's active cart at Dragon Palace
INSERT INTO carts (id, user_id, restaurant_id, total_amount, active, created_at, updated_at)
VALUES ('c2222222-2222-2222-2222-222222222222'::uuid, '33333333-3333-3333-3333-333333333333'::uuid, 
        'a2222222-2222-2222-2222-222222222222'::uuid, 607.00, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Cart items for Priya
INSERT INTO cart_items (id, cart_id, menu_item_id, quantity, price_per_item, total_price)
VALUES ('c2222222-2222-2222-2222-111111111111'::uuid, 'c2222222-2222-2222-2222-222222222222'::uuid, 
        'b2222222-2222-2222-2222-111111111111'::uuid, 2, 279.00, 558.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO cart_items (id, cart_id, menu_item_id, quantity, price_per_item, total_price)
VALUES ('c2222222-2222-2222-2222-222222222223'::uuid, 'c2222222-2222-2222-2222-222222222222'::uuid, 
        'b2222222-2222-2222-2222-333333333333'::uuid, 1, 149.00, 149.00)
ON CONFLICT (id) DO NOTHING;
