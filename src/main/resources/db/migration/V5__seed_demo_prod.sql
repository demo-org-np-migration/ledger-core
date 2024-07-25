-- Keycloak exige id de usuario único en toda la instancia y staging/prod
-- comparten el mismo Keycloak, así que el realm de prod usa otro juego de
-- ids para los mismos usuarios demo (las convenciones internas de API, "Prod usa IDs
-- distintos"). Esta migración corre en las dos bases (staging y prod tienen
-- el mismo seed), así que sembramos el juego de prod acá sin tocar V4.
INSERT INTO customers (id, email, full_name, kyc_status, created_at) VALUES
    ('aaaaaaaa-0000-4000-8000-000000000011', 'ana.cliente+prod@example.com', 'Ana Cliente', 'approved', now()),
    ('bbbbbbbb-0000-4000-8000-000000000012', 'bruno.comercio+prod@example.com', 'Bruno Comercio', 'approved', now());

INSERT INTO accounts (id, customer_id, currency, balance, status, created_at) VALUES
    ('aaaaaaaa-0000-4000-8000-000000000111', 'aaaaaaaa-0000-4000-8000-000000000011', 'ARS', 50000.00, 'active', now()),
    ('aaaaaaaa-0000-4000-8000-000000000112', 'aaaaaaaa-0000-4000-8000-000000000011', 'USD', 500.00, 'active', now()),
    ('bbbbbbbb-0000-4000-8000-000000000211', 'bbbbbbbb-0000-4000-8000-000000000012', 'ARS', 0.00, 'active', now());
