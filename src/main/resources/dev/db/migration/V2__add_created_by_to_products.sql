ALTER TABLE products
    ADD COLUMN created_by BIGINT;

-- Add foreign key constraint
ALTER TABLE products
    ADD CONSTRAINT fk_products_created_by
        FOREIGN KEY (created_by)
            REFERENCES accounts(id);

-- Add index for better query performance
CREATE INDEX idx_products_created_by ON products(created_by);