-- Added when accounts-api needed to freeze accounts (las convenciones internas de API,
-- "POST /v1/accounts/{id}/freeze"). Existing rows default to active.
ALTER TABLE accounts ADD COLUMN status TEXT NOT NULL DEFAULT 'active';
ALTER TABLE accounts ADD CONSTRAINT accounts_status_check CHECK (status IN ('active', 'frozen', 'closed'));
