CREATE EXTENSION IF NOT EXISTS vector;

-- =============================================
-- Step 2: Add vector column to products table
-- =============================================
ALTER TABLE products
    ADD COLUMN description_vector vector(1536);
