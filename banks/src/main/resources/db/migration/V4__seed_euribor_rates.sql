-- Valores de referencia sinteticos para sandbox/demo.
-- No son fuente oficial ni recomendacion financiera.
SET search_path TO banks;

INSERT INTO euribor_rates (period, rate_12m_pct, source) VALUES
    (DATE '2026-01-01', 2.1450, 'DEMO'),
    (DATE '2026-02-01', 2.0800, 'DEMO'),
    (DATE '2026-03-01', 2.0200, 'DEMO'),
    (DATE '2026-04-01', 1.9700, 'DEMO'),
    (DATE '2026-05-01', 1.9300, 'DEMO')
ON CONFLICT (period) DO UPDATE SET
    rate_12m_pct = EXCLUDED.rate_12m_pct,
    source = EXCLUDED.source;
