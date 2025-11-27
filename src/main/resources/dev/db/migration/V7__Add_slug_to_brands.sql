-- ================================================
-- Migration: Add "slug" column to brands table
-- ================================================

ALTER TABLE brands
    ADD COLUMN slug VARCHAR(255);

-- Optional: set slug = lower(name) replace spaces → only for existing data
-- Nếu bạn muốn tự generate slug cho dữ liệu hiện có:
UPDATE brands
SET slug = lower(regexp_replace(name, '\s+', '-', 'g'))
WHERE slug IS NULL;
