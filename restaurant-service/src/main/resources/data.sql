-- Restaurant Service Seed Data
-- Fixed UUIDs for cross-service reference

-- Indian Restaurants
INSERT INTO restaurants (id, name, address, phone, cuisine_type, status, active, created_at, updated_at)
VALUES ('a1111111-1111-1111-1111-111111111111'::uuid, 'Spice Garden', 'MG Road, Bangalore', '9800000001', 
        'INDIAN', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO restaurants (id, name, address, phone, cuisine_type, status, active, created_at, updated_at)
VALUES ('a1111111-2222-2222-2222-222222222222'::uuid, 'Tandoori Nights', 'Koramangala, Bangalore', '9800000002', 
        'INDIAN', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Chinese Restaurant
INSERT INTO restaurants (id, name, address, phone, cuisine_type, status, active, created_at, updated_at)
VALUES ('a2222222-2222-2222-2222-222222222222'::uuid, 'Dragon Palace', 'Indiranagar, Bangalore', '9800000003', 
        'CHINESE', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Italian Restaurant
INSERT INTO restaurants (id, name, address, phone, cuisine_type, status, active, created_at, updated_at)
VALUES ('a3333333-3333-3333-3333-333333333333'::uuid, 'Pizza Italia', 'HSR Layout, Bangalore', '9800000004', 
        'ITALIAN', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Fast Food
INSERT INTO restaurants (id, name, address, phone, cuisine_type, status, active, created_at, updated_at)
VALUES ('a4444444-4444-4444-4444-444444444444'::uuid, 'Burger Barn', 'Whitefield, Bangalore', '9800000005', 
        'FAST_FOOD', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Street Food
INSERT INTO restaurants (id, name, address, phone, cuisine_type, status, active, created_at, updated_at)
VALUES ('a5555555-5555-5555-5555-555555555555'::uuid, 'Chaat Corner', 'Jayanagar, Bangalore', '9800000006', 
        'STREET_FOOD', 'ACTIVE', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Pending Restaurant (for testing)
INSERT INTO restaurants (id, name, address, phone, cuisine_type, status, active, created_at, updated_at)
VALUES ('a6666666-6666-6666-6666-666666666666'::uuid, 'New Kitchen', 'BTM Layout, Bangalore', '9800000007', 
        'CONTINENTAL', 'PENDING', false, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
