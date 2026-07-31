-- ================================================================
-- V2__seed_data.sql
-- Seeds baseline reference data so the app is usable immediately
-- after startup (categories + a couple of demo products/users).
-- Password hash below is BCrypt for the plaintext "password123".
-- ================================================================

-- Top-level categories first, then children referencing their parent by name.
-- Using subqueries instead of hardcoded IDs so this seed doesn't depend on
-- insert order or auto-increment starting at any particular value.
INSERT INTO categories (name, description, parent_id) VALUES
    ('Electronics', 'Electronic devices and accessories', NULL),
    ('Home & Kitchen', 'Home and kitchen appliances', NULL),
    ('Furniture', 'Home and office furniture', NULL);

INSERT INTO categories (name, description, parent_id)
SELECT 'Audio', 'Headphones, speakers and audio gear', id FROM categories WHERE name = 'Electronics'
UNION ALL
SELECT 'Cables & Chargers', 'Cables, adapters and chargers', id FROM categories WHERE name = 'Electronics'
UNION ALL
SELECT 'Wearables', 'Smartwatches and fitness trackers', id FROM categories WHERE name = 'Electronics'
UNION ALL
SELECT 'Computer Accessories', 'Keyboards, mice and peripherals', id FROM categories WHERE name = 'Electronics'
UNION ALL
SELECT 'Small Appliances', 'Compact kitchen and home gadgets', id FROM categories WHERE name = 'Home & Kitchen'
UNION ALL
SELECT 'Cookware', 'Pots, pans and cooking essentials', id FROM categories WHERE name = 'Home & Kitchen'
UNION ALL
SELECT 'Office', 'Desks, chairs and office supplies', id FROM categories WHERE name = 'Furniture';

-- Password hash below is BCrypt for the plaintext "password123" for every seeded user.
INSERT INTO users (username, email, password, first_name, last_name, role) VALUES
    ('admin',   'admin@ecommerce.com',   '$2a$10$7EqJtq98hPqEX7fNZaFWoOa2Q9dK2SR7Xq7X6Xd3XJ8yq0m5xVYeK', 'Admin', 'User',   'ADMIN'),
    ('jdoe',    'jdoe@example.com',      '$2a$10$7EqJtq98hPqEX7fNZaFWoOa2Q9dK2SR7Xq7X6Xd3XJ8yq0m5xVYeK', 'John',  'Doe',    'CUSTOMER'),
    ('asmith',  'asmith@example.com',    '$2a$10$7EqJtq98hPqEX7fNZaFWoOa2Q9dK2SR7Xq7X6Xd3XJ8yq0m5xVYeK', 'Alice', 'Smith',  'CUSTOMER'),
    ('mgarcia', 'mgarcia@example.com',   '$2a$10$7EqJtq98hPqEX7fNZaFWoOa2Q9dK2SR7Xq7X6Xd3XJ8yq0m5xVYeK', 'Maria', 'Garcia', 'CUSTOMER');

INSERT INTO products (name, description, price, stock, category_id, image_url)
SELECT 'Wireless Headphones', 'Over-ear noise-cancelling headphones', 99.99, 100, id, '/images/headphones.jpg' FROM categories WHERE name = 'Audio'
UNION ALL
SELECT 'Earbuds Pro', 'True wireless earbuds with ANC', 129.99, 150, id, '/images/earbuds-pro.jpg' FROM categories WHERE name = 'Audio'
UNION ALL
SELECT 'Bluetooth Speaker', 'Portable waterproof speaker', 49.99, 60, id, '/images/speaker.jpg' FROM categories WHERE name = 'Audio'
UNION ALL
SELECT 'Soundbar 2.1', 'Compact soundbar with wireless subwoofer', 149.99, 35, id, '/images/soundbar.jpg' FROM categories WHERE name = 'Audio'
UNION ALL
SELECT 'USB-C Cable 1m', 'Braided USB-C to USB-C charging cable', 9.99, 300, id, '/images/usbc-cable.jpg' FROM categories WHERE name = 'Cables & Chargers'
UNION ALL
SELECT 'USB-C Cable 2m', 'Braided USB-C to USB-C charging cable, extra length', 12.99, 250, id, '/images/usbc-cable-2m.jpg' FROM categories WHERE name = 'Cables & Chargers'
UNION ALL
SELECT '65W GaN Charger', 'Compact fast charger with dual USB-C ports', 34.99, 180, id, '/images/gan-charger.jpg' FROM categories WHERE name = 'Cables & Chargers'
UNION ALL
SELECT 'Wireless Charging Pad', '15W Qi wireless charging pad', 24.99, 200, id, '/images/charging-pad.jpg' FROM categories WHERE name = 'Cables & Chargers'
UNION ALL
SELECT 'Smartwatch Series 5', 'Fitness tracking smartwatch with heart rate monitor', 199.99, 70, id, '/images/smartwatch.jpg' FROM categories WHERE name = 'Wearables'
UNION ALL
SELECT 'Fitness Band', 'Lightweight activity and sleep tracker', 39.99, 220, id, '/images/fitness-band.jpg' FROM categories WHERE name = 'Wearables'
UNION ALL
SELECT 'Mechanical Keyboard', 'RGB backlit mechanical gaming keyboard', 89.99, 90, id, '/images/keyboard.jpg' FROM categories WHERE name = 'Computer Accessories'
UNION ALL
SELECT 'Wireless Mouse', 'Ergonomic wireless mouse with silent clicks', 24.99, 150, id, '/images/mouse.jpg' FROM categories WHERE name = 'Computer Accessories'
UNION ALL
SELECT 'Laptop Stand', 'Adjustable aluminum laptop stand', 29.99, 130, id, '/images/laptop-stand.jpg' FROM categories WHERE name = 'Computer Accessories'
UNION ALL
SELECT 'Smart Kettle', 'Wi-Fi enabled electric kettle', 79.99, 40, id, '/images/kettle.jpg' FROM categories WHERE name = 'Small Appliances'
UNION ALL
SELECT 'Toaster 2-Slice', 'Stainless steel 2-slice toaster', 34.99, 85, id, '/images/toaster.jpg' FROM categories WHERE name = 'Small Appliances'
UNION ALL
SELECT 'Air Fryer 4L', 'Digital air fryer with 8 presets', 89.99, 55, id, '/images/air-fryer.jpg' FROM categories WHERE name = 'Small Appliances'
UNION ALL
SELECT 'Coffee Maker', '12-cup programmable drip coffee maker', 59.99, 65, id, '/images/coffee-maker.jpg' FROM categories WHERE name = 'Small Appliances'
UNION ALL
SELECT 'Non-Stick Pan Set', '3-piece non-stick frying pan set', 44.99, 75, id, '/images/pan-set.jpg' FROM categories WHERE name = 'Cookware'
UNION ALL
SELECT 'Stainless Steel Pot', '5-quart stainless steel stock pot', 39.99, 60, id, '/images/stock-pot.jpg' FROM categories WHERE name = 'Cookware'
UNION ALL
SELECT 'Knife Set', '6-piece kitchen knife set with block', 49.99, 45, id, '/images/knife-set.jpg' FROM categories WHERE name = 'Cookware'
UNION ALL
SELECT 'Office Chair', 'Ergonomic mesh office chair with lumbar support', 159.99, 30, id, '/images/office-chair.jpg' FROM categories WHERE name = 'Office'
UNION ALL
SELECT 'Standing Desk', 'Electric height-adjustable standing desk', 349.99, 20, id, '/images/standing-desk.jpg' FROM categories WHERE name = 'Office'
UNION ALL
SELECT 'Desk Lamp', 'LED desk lamp with adjustable brightness', 19.99, 140, id, '/images/desk-lamp.jpg' FROM categories WHERE name = 'Office';
