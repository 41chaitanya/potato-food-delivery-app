-- User Auth Service Seed Data
-- Password is 'password123' encoded with BCrypt

-- Admin User
INSERT INTO users (id, name, email, password, role, phone, address, active, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111'::uuid, 'Admin User', 'admin@foodapp.com', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 
        'ADMIN', '9876543210', 'Admin Office, Tech Park', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Regular Users
INSERT INTO users (id, name, email, password, role, phone, address, active, created_at, updated_at)
VALUES ('22222222-2222-2222-2222-222222222222'::uuid, 'Rahul Sharma', 'rahul@gmail.com', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 
        'USER', '9876543211', '123 MG Road, Bangalore', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, name, email, password, role, phone, address, active, created_at, updated_at)
VALUES ('33333333-3333-3333-3333-333333333333'::uuid, 'Priya Patel', 'priya@gmail.com', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 
        'USER', '9876543212', '456 Koramangala, Bangalore', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, name, email, password, role, phone, address, active, created_at, updated_at)
VALUES ('44444444-4444-4444-4444-444444444444'::uuid, 'Amit Kumar', 'amit@gmail.com', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 
        'USER', '9876543213', '789 Indiranagar, Bangalore', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Delivery Riders
INSERT INTO users (id, name, email, password, role, phone, address, active, created_at, updated_at)
VALUES ('55555555-5555-5555-5555-555555555555'::uuid, 'Raju Rider', 'raju.rider@foodapp.com', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 
        'RIDER', '9876543214', 'HSR Layout, Bangalore', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, name, email, password, role, phone, address, active, created_at, updated_at)
VALUES ('66666666-6666-6666-6666-666666666666'::uuid, 'Suresh Delivery', 'suresh.rider@foodapp.com', 
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsS8gPvjWfCyhP3m6e', 
        'RIDER', '9876543215', 'Whitefield, Bangalore', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
