-- Habilitar a extensão pgcrypto para UUIDs (se necessário)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Criar Tabela de Produtos
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);

-- Habilitar Row Level Security (RLS)
ALTER TABLE products ENABLE ROW LEVEL SECURITY;

-- Política de Isolamento: Um usuário só vê e só CRIA dados do seu próprio Tenant
CREATE POLICY tenant_isolation_policy ON products
    USING (tenant_id = current_setting('app.current_tenant'))
    WITH CHECK (tenant_id = current_setting('app.current_tenant'));

-- Inserir dados de teste para diferentes Tenants
INSERT INTO products (name, price, tenant_id) VALUES ('Iphone 15 (Tenant A)', 5000, 'TENANT_A');
INSERT INTO products (name, price, tenant_id) VALUES ('Macbook Pro (Tenant B)', 15000, 'TENANT_B');
