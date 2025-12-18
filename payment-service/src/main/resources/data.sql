-- Payment Service Seed Data
-- Sample payments linked to orders

INSERT INTO payments_tb (id, order_id, amount, status, payment_time)
VALUES ('a1111111-1111-1111-1111-111111111111'::uuid, '01111111-1111-1111-1111-111111111111'::uuid, 
        547.00, 'SUCCESS', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments_tb (id, order_id, amount, status, payment_time)
VALUES ('a2222222-2222-2222-2222-222222222222'::uuid, '02222222-2222-2222-2222-222222222222'::uuid, 
        458.00, 'SUCCESS', NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments_tb (id, order_id, amount, status, payment_time)
VALUES ('a3333333-3333-3333-3333-333333333333'::uuid, '03333333-3333-3333-3333-333333333333'::uuid, 
        648.00, 'SUCCESS', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments_tb (id, order_id, amount, status, payment_time)
VALUES ('a4444444-4444-4444-4444-444444444444'::uuid, '04444444-4444-4444-4444-444444444444'::uuid, 
        298.00, 'SUCCESS', NOW())
ON CONFLICT (id) DO NOTHING;
