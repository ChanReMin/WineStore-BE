-- ================================================
-- Migration: Update warehouses table structure
-- ================================================

-- 1. Thay đổi độ dài name
ALTER TABLE warehouses
ALTER COLUMN name TYPE VARCHAR(255);

-- 2. Thay đổi độ dài location
ALTER TABLE warehouses
ALTER COLUMN location TYPE VARCHAR(512);

-- 3. Chuyển description sang TEXT
ALTER TABLE warehouses
ALTER COLUMN description TYPE TEXT;

-- 4. Thêm cột status (enum ordinal)
ALTER TABLE warehouses
    ADD COLUMN status SMALLINT DEFAULT 0 NOT NULL;

-- 5. Thêm approval / rejection / ban fields
ALTER TABLE warehouses
    ADD COLUMN approval_note VARCHAR(1000),
    ADD COLUMN rejection_reason VARCHAR(1000),
    ADD COLUMN ban_reason VARCHAR(1000);

-- 6. Thêm thời gian approve
ALTER TABLE warehouses
    ADD COLUMN approved_at TIMESTAMP;

-- 7. Thêm approved_by (FK)
ALTER TABLE warehouses
    ADD COLUMN approved_by BIGINT;

ALTER TABLE warehouses
    ADD CONSTRAINT fk_warehouse_approved_by
        FOREIGN KEY (approved_by) REFERENCES accounts(id);

-- 8. Thêm indexes mới
CREATE INDEX IF NOT EXISTS idx_manager_id ON warehouses(manager_id);
CREATE INDEX IF NOT EXISTS idx_status ON warehouses(status);
CREATE INDEX IF NOT EXISTS idx_created_at ON warehouses(created_at);
