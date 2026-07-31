-- ================================================================
-- V1__initial_schema.sql
-- Creates the core e-commerce schema: users, categories, products,
-- orders, order_items, payments.
-- ================================================================

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    role            VARCHAR(20)  NOT NULL DEFAULT 'CUSTOMER',
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

-- Self-referencing FK gives categories a hierarchical (tree) structure
CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    parent_id       BIGINT REFERENCES categories(id),
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200)   NOT NULL,
    description     TEXT,
    price           NUMERIC(10,2)  NOT NULL CHECK (price >= 0),
    stock           INTEGER        NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category_id     BIGINT         NOT NULL REFERENCES categories(id),
    image_url       VARCHAR(500),
    is_active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE TABLE orders (
    id                BIGSERIAL PRIMARY KEY,
    order_number      VARCHAR(50)   NOT NULL UNIQUE,
    user_id           BIGINT        NOT NULL REFERENCES users(id),
    total_amount      NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0),
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    shipping_address  TEXT,
    created_at        TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id      BIGINT        NOT NULL REFERENCES products(id),
    quantity        INTEGER       NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(10,2) NOT NULL CHECK (unit_price >= 0),
    subtotal        NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0)
);

CREATE TABLE payments (
    id                BIGSERIAL PRIMARY KEY,
    order_id          BIGINT        NOT NULL UNIQUE REFERENCES orders(id),
    amount            NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
    payment_method    VARCHAR(30)   NOT NULL,
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    transaction_ref   VARCHAR(100),
    created_at        TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT now()
);
