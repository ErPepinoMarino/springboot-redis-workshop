INSERT INTO products (name, description, price)
SELECT
    (ARRAY['Widget', 'Gadget', 'Gizmo', 'Doohickey', 'Thingamajig'])[1 + (g % 5)] || ' #' || g,
    'Producto de prueba numero ' || g,
    (((g % 500) + 1)::numeric / 10)
FROM generate_series(1, 20000) AS g;