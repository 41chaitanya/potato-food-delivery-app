-- Admin Service Seed Data

-- Commission Configuration
INSERT INTO commission_config (id, config_key, commission_percentage, description, active, created_at, updated_at)
VALUES ('ad111111-1111-1111-1111-111111111111'::uuid, 'DEFAULT_COMMISSION', 15.00, 
        'Default platform commission for all restaurants', true, NOW(), NOW())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO commission_config (id, config_key, commission_percentage, description, active, created_at, updated_at)
VALUES ('ad222222-2222-2222-2222-222222222222'::uuid, 'PREMIUM_COMMISSION', 12.00, 
        'Reduced commission for premium partner restaurants', true, NOW(), NOW())
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO commission_config (id, config_key, commission_percentage, description, active, created_at, updated_at)
VALUES ('ad333333-3333-3333-3333-333333333333'::uuid, 'NEW_RESTAURANT_COMMISSION', 10.00, 
        'Promotional commission for new restaurants (first 3 months)', true, NOW(), NOW())
ON CONFLICT (config_key) DO NOTHING;

-- Platform Stats (Sample historical data)
INSERT INTO platform_stats (id, stats_date, total_users, total_restaurants, total_riders, total_orders, completed_orders, cancelled_orders, total_revenue, total_commission, created_at, updated_at)
VALUES ('e1111111-1111-1111-1111-111111111111'::uuid, '2024-12-15', 100, 7, 5, 250, 230, 20, 75000.00, 11250.00, NOW(), NOW())
ON CONFLICT (stats_date) DO NOTHING;

INSERT INTO platform_stats (id, stats_date, total_users, total_restaurants, total_riders, total_orders, completed_orders, cancelled_orders, total_revenue, total_commission, created_at, updated_at)
VALUES ('e2222222-2222-2222-2222-222222222222'::uuid, '2024-12-16', 105, 7, 5, 280, 265, 15, 84000.00, 12600.00, NOW(), NOW())
ON CONFLICT (stats_date) DO NOTHING;

INSERT INTO platform_stats (id, stats_date, total_users, total_restaurants, total_riders, total_orders, completed_orders, cancelled_orders, total_revenue, total_commission, created_at, updated_at)
VALUES ('e3333333-3333-3333-3333-333333333333'::uuid, '2024-12-17', 110, 7, 6, 310, 295, 15, 93000.00, 13950.00, NOW(), NOW())
ON CONFLICT (stats_date) DO NOTHING;
