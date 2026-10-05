INSERT INTO products (id, name, description, price) VALUES
    (1, 'Teclado mecánico', 'Switches azules, retroiluminado', 49.90),
    (2, 'Ratón inalámbrico', '6 botones, 16000 DPI', 29.95),
    (3, 'Monitor 27 pulgadas', '2K, 165 Hz', 289.00);

ALTER TABLE products ALTER COLUMN id RESTART WITH 4; 