UPDATE promotions
SET used_count = 0
WHERE used_count IS NULL;

ALTER TABLE promotions
    MODIFY COLUMN used_count INT NOT NULL DEFAULT 0;