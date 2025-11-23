ALTER TABLE products
    ADD COLUMN status SMALLINT NOT NULL DEFAULT 0;

-- Add check constraint để đảm bảo status hợp lệ
-- 0 = PENDING, 1 = ACTIVE, 2 = BAN
ALTER TABLE products
    ADD CONSTRAINT chk_products_status
        CHECK (status IN (0, 1, 2));

ALTER TABLE products
    ADD COLUMN approved_at TIMESTAMP;

ALTER TABLE products
    ADD COLUMN approved_by BIGINT;

-- Add foreign key constraint to accounts table
ALTER TABLE products
    ADD CONSTRAINT fk_products_approved_by
        FOREIGN KEY (approved_by)
            REFERENCES accounts(id)
            ON DELETE SET NULL;