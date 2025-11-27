-- ================================================
-- Migration: Update products table structure
-- ================================================

-- 1. Thêm các cột SEO / slug / SKU
ALTER TABLE products
    ADD COLUMN slug VARCHAR(200),
    ADD COLUMN sku VARCHAR(100),
    ADD COLUMN cost_price NUMERIC(15,2),
    ADD COLUMN original_price NUMERIC(15,2),
    ADD COLUMN origin_region VARCHAR(200),
    ADD COLUMN vintage_year INTEGER,
    ADD COLUMN serving_temperature VARCHAR(50),
    ADD COLUMN food_pairing TEXT,
    ADD COLUMN taste_profile TEXT,
    ADD COLUMN full_description TEXT,
    ADD COLUMN rating_average NUMERIC(3,2),
    ADD COLUMN rating_count INTEGER,
    ADD COLUMN sold_count INTEGER,
    ADD COLUMN meta_title VARCHAR(200);


-- 4. Các index cũ (đảm bảo không bị mất)
CREATE INDEX IF NOT EXISTS idx_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_brand_id ON products(brand_id);
CREATE INDEX IF NOT EXISTS idx_wine_type ON products(wine_type);
CREATE INDEX IF NOT EXISTS idx_created_year ON products(created_at);
