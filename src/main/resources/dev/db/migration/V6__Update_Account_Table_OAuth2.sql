ALTER TABLE accounts
    ADD COLUMN provider VARCHAR(20);

ALTER TABLE accounts
    ADD COLUMN provider_id VARCHAR(255);