INSERT INTO products (id, name, description, price) VALUES
    (4, 'Auriculares Bluetooth', 'Cancelación de ruido, 30 h', 89.90),
    (5, 'Alfombrilla XL', '90x40 cm', 24.99),
    (6, 'Silla ergonómica', 'Malla, soporte lumbar', 199.00),
    (7, 'Lámpara de escritorio', 'LED regulable', 34.50),
    (8, 'Hub USB-C', '7 puertos', 39.99),
    (9, 'Disco SSD 1 TB', 'NVMe', 99.95),
    (10, 'Webcam 1080p', '60 fps', 59.90),
    (11, 'Micrófono USB', 'Patrón cardioide', 75.00),
    (12, 'Altavoces 2.0', '20 W', 45.00),
    (13, 'Soporte de monitor', 'Ajustable en altura', 39.90),
    (14, 'Teclado inalámbrico', 'Formato TKL', 64.90),
    (15, 'Ratón vertical', 'Ergonómico', 34.95),
    (16, 'Cargador rápido 65W', 'GaN', 29.90),
    (17, 'Batería externa', '20000 mAh', 44.90),
    (18, 'Tableta gráfica', '10 pulgadas', 79.00),
    (19, 'Reposapiés', 'Ajustable', 19.90),
    (20, 'Organizador de cables', '10 piezas', 12.50);

ALTER TABLE products ALTER COLUMN id RESTART WITH 21;