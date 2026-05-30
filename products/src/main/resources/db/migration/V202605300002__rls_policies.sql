-- ============================================================
-- V202605300002__rls_policies.sql
-- Row Level Security — products schema
-- ============================================================
SET search_path TO products;

-- Properties
ALTER TABLE properties ENABLE ROW LEVEL SECURITY;

CREATE POLICY prop_tenant_isolation ON properties
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE POLICY prop_tenant_insert ON properties
    FOR INSERT
    WITH CHECK (tenant_id = current_setting('app.tenant_id', true)::uuid);

-- Transactions
ALTER TABLE transactions ENABLE ROW LEVEL SECURITY;

CREATE POLICY tx_tenant_isolation ON transactions
    USING (tenant_id = current_setting('app.tenant_id', true)::uuid);

CREATE POLICY tx_tenant_insert ON transactions
    FOR INSERT
    WITH CHECK (tenant_id = current_setting('app.tenant_id', true)::uuid);

-- Grant the application role bypass only via interceptor setting
-- (app role uses SET LOCAL app.tenant_id = '<uuid>' at connection start)
