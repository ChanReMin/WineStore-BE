-- Convert null values
UPDATE promotions
SET used_count = 0
WHERE used_count IS NULL;

-- Set default 0
ALTER TABLE promotions
    ALTER COLUMN used_count SET DEFAULT 0;

-- Set NOT NULL constraint
ALTER TABLE promotions
    ALTER COLUMN used_count SET NOT NULL;
