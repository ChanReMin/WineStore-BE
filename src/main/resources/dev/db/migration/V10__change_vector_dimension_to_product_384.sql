ALTER TABLE products
DROP COLUMN description_vector;

ALTER TABLE products
    ADD COLUMN description_vector vector(384);
