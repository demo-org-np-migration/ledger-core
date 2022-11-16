-- Fixed demo IDs shared with the Keycloak realm JSON and payments-api's own
-- seed, so the traffic generator and the SPAs work without manual steps
-- (las convenciones internas de API).
INSERT INTO customers (id, email, full_name, kyc_status, created_at) VALUES
    ('aaaaaaaa-0000-4000-8000-000000000001', 'ana.cliente@cauri.io', 'Ana Cliente', 'approved', now()),
    ('bbbbbbbb-0000-4000-8000-000000000002', 'bruno.comercio@cauri.io', 'Bruno Comercio', 'approved', now());

INSERT INTO accounts (id, customer_id, currency, balance, status, created_at) VALUES
    ('aaaaaaaa-0000-4000-8000-000000000101', 'aaaaaaaa-0000-4000-8000-000000000001', 'ARS', 50000.00, 'active', now()),
    ('aaaaaaaa-0000-4000-8000-000000000102', 'aaaaaaaa-0000-4000-8000-000000000001', 'USD', 500.00, 'active', now()),
    ('bbbbbbbb-0000-4000-8000-000000000201', 'bbbbbbbb-0000-4000-8000-000000000002', 'ARS', 0.00, 'active', now()),
    ('bbbbbbbb-0000-4000-8000-000000000202', 'bbbbbbbb-0000-4000-8000-000000000002', 'ARS', 0.00, 'active', now());
