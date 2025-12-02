-- 1. Add temp integer column
ALTER TABLE inventory_log
    ADD COLUMN type_int SMALLINT;

-- 2. Convert existing string values to integer codes
UPDATE inventory_log
SET type_int =
        CASE type
            WHEN 'IN' THEN 0
            WHEN 'OUT' THEN 1
            WHEN 'ADJUST' THEN 2
            WHEN 'RETURN' THEN 3
            WHEN 'TRANSFER_OUT' THEN 4
            WHEN 'TRANSFER_IN' THEN 5
            ELSE 0
            END;

-- 3. Drop string column
ALTER TABLE inventory_log DROP COLUMN type;

-- 4. Rename temp column to original name
ALTER TABLE inventory_log RENAME COLUMN type_int TO type;
