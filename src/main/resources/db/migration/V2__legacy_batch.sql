-- Backs the daily-close batch that predates reporting-etl. Dead module,
-- kept as-is (D7, ledger-core spec "Defectos a plantar").
CREATE TABLE legacy_batch_runs (
    id SERIAL PRIMARY KEY,
    ran_at TIMESTAMP WITH TIME ZONE,
    rows INT
);
