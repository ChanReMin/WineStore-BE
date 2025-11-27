-- 1) Normalize existing status values to upper-case (for enum mapping)
UPDATE notifications
SET status = UPPER(status)
WHERE status IS NOT NULL;

-- 2) Set NULL is_read to false
UPDATE notifications
SET is_read = FALSE
WHERE is_read IS NULL;

-- 3) Make is_read NOT NULL with default false
ALTER TABLE notifications
    ALTER COLUMN is_read SET DEFAULT FALSE,
    ALTER COLUMN is_read SET NOT NULL;

-- 4) Add item_url column
ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS item_url VARCHAR(500);
