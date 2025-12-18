-- Delivery Service Seed Data
-- Sample deliveries linked to orders and riders

INSERT INTO deliveries (id, order_id, rider_id, status, assigned_at, picked_up_at, delivered_at)
VALUES ('de111111-1111-1111-1111-111111111111'::uuid, '01111111-1111-1111-1111-111111111111'::uuid, 
        '55555555-5555-5555-5555-555555555555'::uuid, 'DELIVERED', 
        NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '15 minutes', NOW() - INTERVAL '2 days' + INTERVAL '45 minutes')
ON CONFLICT (id) DO NOTHING;

INSERT INTO deliveries (id, order_id, rider_id, status, assigned_at, picked_up_at, delivered_at)
VALUES ('de222222-2222-2222-2222-222222222222'::uuid, '02222222-2222-2222-2222-222222222222'::uuid, 
        '66666666-6666-6666-6666-666666666666'::uuid, 'DELIVERED', 
        NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '10 minutes', NOW() - INTERVAL '1 day' + INTERVAL '35 minutes')
ON CONFLICT (id) DO NOTHING;

INSERT INTO deliveries (id, order_id, rider_id, status, assigned_at, picked_up_at)
VALUES ('de333333-3333-3333-3333-333333333333'::uuid, '03333333-3333-3333-3333-333333333333'::uuid, 
        '55555555-5555-5555-5555-555555555555'::uuid, 'PICKED_UP', 
        NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '15 minutes')
ON CONFLICT (id) DO NOTHING;
