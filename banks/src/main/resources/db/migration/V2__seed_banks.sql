-- Seed demo para Civitas Capital.
-- Datos sinteticos para sandbox: no representan ofertas ni condiciones vinculantes.
SET search_path TO banks;

INSERT INTO banks (
    id, tenant_id, code, name, brand, country, bde_registry_nr, rating,
    logo_url, website_url, active, created_by, updated_by
) VALUES
    ('10000000-0000-4000-8000-000000000001', '11111111-1111-4111-8111-111111111111', 'SANESMM', 'Banco Santander', 'Santander', 'ES', NULL, 'A', NULL, 'https://www.santander.es', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000002', '11111111-1111-4111-8111-111111111111', 'BBVAESMM', 'Banco Bilbao Vizcaya Argentaria', 'BBVA', 'ES', NULL, 'A', NULL, 'https://www.bbva.es', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000003', '11111111-1111-4111-8111-111111111111', 'CAIXESBB', 'CaixaBank', 'CaixaBank', 'ES', NULL, 'A', NULL, 'https://www.caixabank.es', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000004', '11111111-1111-4111-8111-111111111111', 'INGDESMM', 'ING Bank Spain', 'ING', 'ES', NULL, 'A-', NULL, 'https://www.ing.es', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000005', '11111111-1111-4111-8111-111111111111', 'BKBKESMM', 'Bankinter', 'Bankinter', 'ES', NULL, 'A-', NULL, 'https://www.bankinter.com', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000006', '11111111-1111-4111-8111-111111111111', 'EVOBESMM', 'EVO Banco', 'EVO', 'ES', NULL, 'BBB', NULL, 'https://www.evobanco.com', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000007', '11111111-1111-4111-8111-111111111111', 'CAGLESMM', 'Abanca', 'Abanca', 'ES', NULL, 'BBB', NULL, 'https://www.abanca.com', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000008', '11111111-1111-4111-8111-111111111111', 'CCRIESMM', 'Cajamar Caja Rural', 'Cajamar', 'ES', NULL, 'BBB', NULL, 'https://www.cajamar.es', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000009', '11111111-1111-4111-8111-111111111111', 'BASKES2B', 'Kutxabank', 'Kutxabank', 'ES', NULL, 'A-', NULL, 'https://www.kutxabank.es', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000010', '11111111-1111-4111-8111-111111111111', 'MYINESMM', 'MyInvestor Banco', 'MyInvestor', 'ES', NULL, 'BBB', NULL, 'https://myinvestor.es', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000011', '11111111-1111-4111-8111-111111111111', 'OPENESMM', 'Open Bank', 'Openbank', 'ES', NULL, 'A-', NULL, 'https://www.openbank.es', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000012', '11111111-1111-4111-8111-111111111111', 'IMAGINMM', 'ImaginBank', 'Imagin', 'ES', NULL, 'BBB', NULL, 'https://www.imagin.com', TRUE, 'seed', 'seed'),
    ('10000000-0000-4000-8000-000000000013', '11111111-1111-4111-8111-111111111111', 'GOHIESMM', 'GoHipoteca Partner', 'GoHipoteca', 'ES', NULL, 'BBB', NULL, 'https://www.gohipoteca.com', TRUE, 'seed', 'seed')
ON CONFLICT (code) DO NOTHING;

INSERT INTO bank_contact_channels (id, bank_id, type, value, label) VALUES
    ('11000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', 'WEB', 'https://www.santander.es/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', 'WEB', 'https://www.bbva.es/personas/productos/hipotecas.html', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000003', 'WEB', 'https://www.caixabank.es/particular/hipotecas.html', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000004', 'WEB', 'https://www.ing.es/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000005', '10000000-0000-4000-8000-000000000005', 'WEB', 'https://www.bankinter.com/banca/hipotecas-prestamos/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000006', 'WEB', 'https://www.evobanco.com/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000007', '10000000-0000-4000-8000-000000000007', 'WEB', 'https://www.abanca.com/es/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000008', '10000000-0000-4000-8000-000000000008', 'WEB', 'https://www.cajamar.es/es/particulares/productos-y-servicios/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000009', '10000000-0000-4000-8000-000000000009', 'WEB', 'https://portal.kutxabank.es/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000010', '10000000-0000-4000-8000-000000000010', 'WEB', 'https://myinvestor.es/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000011', '10000000-0000-4000-8000-000000000011', 'WEB', 'https://www.openbank.es/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000012', '10000000-0000-4000-8000-000000000012', 'WEB', 'https://www.imagin.com/hipotecas', 'Hipotecas'),
    ('11000000-0000-4000-8000-000000000013', '10000000-0000-4000-8000-000000000013', 'WEB', 'https://www.gohipoteca.com', 'Hipotecas')
ON CONFLICT (id) DO NOTHING;
