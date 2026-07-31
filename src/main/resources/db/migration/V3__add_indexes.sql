-- ================================================================
-- V3__add_indexes.sql
-- Indexes for frequently queried / joined columns, to keep lookups
-- fast as the data set grows (see EXPLAIN ANALYZE plans in README).
-- ================================================================

-- Product lookups by name (search) and by category (catalog browsing)
CREATE INDEX idx_product_name     ON products (name);
CREATE INDEX idx_product_category ON products (category_id);
CREATE INDEX idx_product_active   ON products (is_active) WHERE is_active = TRUE;

-- Order lookups by user (order history) and by status (admin dashboards)
CREATE INDEX idx_order_user       ON orders (user_id);
CREATE INDEX idx_order_status     ON orders (status);
CREATE INDEX idx_order_created_at ON orders (created_at);

-- Order items joined back to their order and product very frequently
CREATE INDEX idx_order_item_order   ON order_items (order_id);
CREATE INDEX idx_order_item_product ON order_items (product_id);

-- Payment lookup by status (reconciliation jobs)
CREATE INDEX idx_payment_status ON payments (status);

-- Category hierarchy traversal
CREATE INDEX idx_category_parent ON categories (parent_id);
