-- V1__Init_Database.sql
-- Flyway Migration Script for Wine E-commerce System

-- =====================================================
-- Table: accounts
-- =====================================================
CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          role SMALLINT,
                          status SMALLINT,
                          refresh_token TEXT,
                          last_login_at TIMESTAMP,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_accounts_email ON accounts(email);
CREATE INDEX idx_accounts_role ON accounts(role);
CREATE INDEX idx_accounts_status ON accounts(status);

-- =====================================================
-- Table: users
-- =====================================================
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       account_id BIGINT NOT NULL UNIQUE,
                       avatar VARCHAR(500),
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       phone_number VARCHAR(20),
                       date_of_birth DATE,
                       gender SMALLINT,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_users_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_account_id ON users(account_id);
CREATE INDEX idx_users_phone ON users(phone_number);

-- =====================================================
-- Table: user_addresses
-- =====================================================
CREATE TABLE user_addresses (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL,
                                full_name VARCHAR(200) NOT NULL,
                                phone_number VARCHAR(20) NOT NULL,
                                address_line VARCHAR(500) NOT NULL,
                                city VARCHAR(100),
                                state VARCHAR(100),
                                country VARCHAR(100),
                                is_default BOOLEAN DEFAULT FALSE,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                deleted_at TIMESTAMP,
                                CONSTRAINT fk_user_addresses_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_id ON user_addresses(user_id);
CREATE INDEX idx_is_default ON user_addresses(is_default);
CREATE INDEX idx_user_addresses_deleted_at ON user_addresses(deleted_at);

-- =====================================================
-- Table: categories
-- =====================================================
CREATE TABLE categories (
                            id BIGSERIAL PRIMARY KEY,
                            parent_id BIGINT,
                            name VARCHAR(200) NOT NULL,
                            slug VARCHAR(250) NOT NULL UNIQUE,
                            description VARCHAR(1000),
                            sort_order INTEGER,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            deleted_at TIMESTAMP,
                            CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE INDEX idx_slug ON categories(slug);
CREATE INDEX idx_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_deleted_at ON categories(deleted_at);

-- =====================================================
-- Table: brands
-- =====================================================
CREATE TABLE brands (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(200) NOT NULL,
                        country VARCHAR(100),
                        description VARCHAR(1000),
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP
);

CREATE INDEX idx_brand_name ON brands(name);
CREATE INDEX idx_brands_deleted_at ON brands(deleted_at);

-- =====================================================
-- Table: products
-- =====================================================
CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          category_id BIGINT NOT NULL,
                          brand_id BIGINT NOT NULL,

                          name VARCHAR(300) NOT NULL,
                          slug VARCHAR(200),
                          sku VARCHAR(100),

                          price DECIMAL(15,2) NOT NULL,
                          cost_price DECIMAL(15,2),
                          original_price DECIMAL(15,2),

                          wine_type VARCHAR(100),
                          country_of_production VARCHAR(100),
                          origin_region VARCHAR(200),
                          grape_variety VARCHAR(200),
                          concentration DECIMAL(5,2),
                          production_area VARCHAR(200),
                          vintage_year INTEGER,

                          capacity INTEGER,
                          ideal_temperature TEXT,
                          serving_temperature VARCHAR(50),
                          humidity TEXT,
                          avoid_light TEXT,
                          place_the_bottle_horizontally TEXT,
                          avoid_vibration TEXT,
                          opened_wine TEXT,
                          use_wine_cabinet TEXT,

                          food_pairing TEXT,
                          taste_profile TEXT,

                          images VARCHAR(1000),
                          description TEXT,
                          full_description TEXT,

                          description_vector vector(1536),

                          status SMALLINT NOT NULL,

                          rating_average DECIMAL(3,2),
                          rating_count INTEGER,
                          sold_count INTEGER,

                          meta_title VARCHAR(200),
                          meta_description VARCHAR(500),

                          approved_at TIMESTAMP,
                          approved_by BIGINT,
                          created_by BIGINT,

                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          deleted_at TIMESTAMP,

                          CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
                          CONSTRAINT fk_products_brand FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE RESTRICT,
                          CONSTRAINT fk_products_approved_by FOREIGN KEY (approved_by) REFERENCES accounts(id) ON DELETE SET NULL,
                          CONSTRAINT fk_products_created_by FOREIGN KEY (created_by) REFERENCES accounts(id) ON DELETE SET NULL,
                          CONSTRAINT uk_product_name_deleted_at UNIQUE (name, deleted_at)
);

CREATE INDEX idx_category_id ON products(category_id);
CREATE INDEX idx_brand_id ON products(brand_id);
CREATE INDEX idx_wine_type ON products(wine_type);
CREATE INDEX idx_created_year ON products(created_at);
CREATE INDEX idx_products_deleted_at ON products(deleted_at);

-- =====================================================
-- Table: warehouses
-- =====================================================
CREATE TABLE warehouses (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(200) NOT NULL,
                            location VARCHAR(500),
                            description VARCHAR(1000),
                            city VARCHAR(100),
                            manager_id BIGINT NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            deleted_at TIMESTAMP,
                            CONSTRAINT fk_warehouses_manager FOREIGN KEY (manager_id) REFERENCES accounts(id) ON DELETE RESTRICT
);

CREATE INDEX idx_warehouses_city ON warehouses(city);
CREATE INDEX idx_warehouses_manager ON warehouses(manager_id);
CREATE INDEX idx_warehouses_deleted_at ON warehouses(deleted_at);

-- =====================================================
-- Table: inventory
-- =====================================================
CREATE TABLE inventory (
                           id BIGSERIAL PRIMARY KEY,
                           warehouse_id BIGINT NOT NULL,
                           product_id BIGINT NOT NULL,
                           quantity_on_hand INTEGER DEFAULT 0,
                           safety_stock INTEGER DEFAULT 0,
                           last_updated_at TIMESTAMP,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           deleted_at TIMESTAMP,
                           CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,
                           CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                           CONSTRAINT uk_product_warehouse UNIQUE (product_id, warehouse_id)
);

CREATE INDEX idx_product_id ON inventory(product_id);
CREATE INDEX idx_warehouse_id ON inventory(warehouse_id);
CREATE INDEX idx_quantity ON inventory(quantity_on_hand);
CREATE INDEX idx_inventory_deleted_at ON inventory(deleted_at);

-- =====================================================
-- Table: inventory_log
-- =====================================================
CREATE TABLE inventory_log (
                               id BIGSERIAL PRIMARY KEY,
                               warehouse_id BIGINT NOT NULL,
                               product_id BIGINT NOT NULL,
                               user_id BIGINT NOT NULL,
                               shipment_id BIGINT,
                               type VARCHAR(20) NOT NULL,
                               quantity INTEGER NOT NULL,
                               note VARCHAR(500),
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               deleted_at TIMESTAMP,
                               CONSTRAINT fk_inventory_log_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,
                               CONSTRAINT fk_inventory_log_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                               CONSTRAINT fk_inventory_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_inventory_log_warehouse_id ON inventory_log(warehouse_id);
CREATE INDEX idx_inventory_log_product_id ON inventory_log(product_id);
CREATE INDEX idx_inventory_log_user_id ON inventory_log(user_id);
CREATE INDEX idx_type ON inventory_log(type);
CREATE INDEX idx_created_at ON inventory_log(created_at);
CREATE INDEX idx_inventory_log_deleted_at ON inventory_log(deleted_at);

-- =====================================================
-- Table: carts
-- =====================================================
CREATE TABLE carts (
                       id BIGSERIAL PRIMARY KEY,
                       user_id BIGINT NOT NULL UNIQUE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       deleted_at TIMESTAMP,
                       CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_deleted_at ON carts(deleted_at);

-- =====================================================
-- Table: cart_items
-- =====================================================
CREATE TABLE cart_items (
                            id BIGSERIAL PRIMARY KEY,
                            cart_id BIGINT NOT NULL,
                            product_id BIGINT NOT NULL,
                            quantity INTEGER NOT NULL DEFAULT 1,
                            unit_price DECIMAL(15, 2) NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            deleted_at TIMESTAMP,
                            CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
                            CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                            CONSTRAINT uk_cart_product UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);
CREATE INDEX idx_cart_items_deleted_at ON cart_items(deleted_at);

-- =====================================================
-- Table: orders
-- =====================================================
CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        shipping_address_id BIGINT NOT NULL,
                        order_code VARCHAR(50) NOT NULL UNIQUE,
                        status SMALLINT NOT NULL,
                        total_amount DECIMAL(15, 2) NOT NULL,
                        discount_amount DECIMAL(15, 2) DEFAULT 0,
                        final_amount DECIMAL(15, 2) NOT NULL,
                        payment_status SMALLINT,
                        paid_at TIMESTAMP,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP,
                        CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
                        CONSTRAINT fk_orders_shipping_address FOREIGN KEY (shipping_address_id) REFERENCES user_addresses(id) ON DELETE RESTRICT
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_code ON orders(order_code);
CREATE INDEX idx_status ON orders(status);
CREATE INDEX idx_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_deleted_at ON orders(deleted_at);

-- =====================================================
-- Table: order_items
-- =====================================================
CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INTEGER NOT NULL,
                             unit_price DECIMAL(15, 2) NOT NULL,
                             line_total DECIMAL(15, 2) NOT NULL,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             deleted_at TIMESTAMP,
                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

CREATE INDEX idx_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_deleted_at ON order_items(deleted_at);

-- =====================================================
-- Table: payment_methods
-- =====================================================
CREATE TABLE payment_methods (
                                 id SERIAL PRIMARY KEY,
                                 code VARCHAR(50) NOT NULL UNIQUE,
                                 name VARCHAR(200) NOT NULL,
                                 description VARCHAR(1000),
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payment_methods_code ON payment_methods(code);

-- =====================================================
-- Table: payment_transactions
-- =====================================================
CREATE TABLE payment_transactions (
                                      id BIGSERIAL PRIMARY KEY,
                                      order_id BIGINT NOT NULL,
                                      payment_method_id INTEGER NOT NULL,
                                      amount DECIMAL(15, 2) NOT NULL,
                                      status SMALLINT NOT NULL,
                                      provider_txn_code VARCHAR(200),
                                      response_data TEXT,
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      deleted_at TIMESTAMP,
                                      CONSTRAINT fk_payment_transactions_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                                      CONSTRAINT fk_payment_transactions_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id) ON DELETE RESTRICT
);

CREATE INDEX idx_payment_transactions_order_id ON payment_transactions(order_id);
CREATE INDEX idx_payment_method_id ON payment_transactions(payment_method_id);
CREATE INDEX idx_payment_transactions_status ON payment_transactions(status);
CREATE INDEX idx_provider_txn_code ON payment_transactions(provider_txn_code);
CREATE INDEX idx_payment_transactions_deleted_at ON payment_transactions(deleted_at);

-- =====================================================
-- Table: promotions
-- =====================================================
CREATE TABLE promotions (
                            id BIGSERIAL PRIMARY KEY,
                            code VARCHAR(50) NOT NULL UNIQUE,
                            name VARCHAR(200) NOT NULL,
                            description VARCHAR(1000),
                            discount_type SMALLINT NOT NULL,
                            discount_value DECIMAL(15, 2) NOT NULL,
                            start_date TIMESTAMP NOT NULL,
                            end_date TIMESTAMP NOT NULL,
                            max_usage INTEGER,
                            used_count INTEGER DEFAULT 0,
                            status SMALLINT,
                            created_by BIGINT NOT NULL,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            deleted_at TIMESTAMP,
                            CONSTRAINT fk_promotions_created_by FOREIGN KEY (created_by) REFERENCES accounts(id) ON DELETE RESTRICT
);

CREATE INDEX idx_code ON promotions(code);
CREATE INDEX idx_promotions_status ON promotions(status);
CREATE INDEX idx_dates ON promotions(start_date, end_date);
CREATE INDEX idx_promotions_deleted_at ON promotions(deleted_at);

-- =====================================================
-- Table: promotion_products
-- =====================================================
CREATE TABLE promotion_products (
                                    id BIGSERIAL PRIMARY KEY,
                                    promotion_id BIGINT NOT NULL,
                                    product_id BIGINT NOT NULL,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    deleted_at TIMESTAMP,
                                    CONSTRAINT fk_promotion_products_promotion FOREIGN KEY (promotion_id) REFERENCES promotions(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_promotion_products_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                                    CONSTRAINT uk_promotion_product UNIQUE (promotion_id, product_id)
);

CREATE INDEX idx_promotion_products_promotion_id ON promotion_products(promotion_id);
CREATE INDEX idx_promotion_products_product_id ON promotion_products(product_id);
CREATE INDEX idx_promotion_products_deleted_at ON promotion_products(deleted_at);

-- =====================================================
-- Table: notifications
-- =====================================================
CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               title VARCHAR(300) NOT NULL,
                               message TEXT NOT NULL,
                               status VARCHAR(20),
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               deleted_at TIMESTAMP,
                               CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_deleted_at ON notifications(deleted_at);
