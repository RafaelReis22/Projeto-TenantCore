-- Criação da Tabela de Produtos (Exemplo de Dados de Cliente)
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    tenant_id VARCHAR(50) NOT NULL -- Identificador do Cliente (Tenant)
);

-- Habilitar Row Level Security (RLS)
ALTER TABLE products ENABLE ROW LEVEL SECURITY;

-- Política de Isolamento: Um usuário só vê e só CRIA dados do seu próprio Tenant
-- A cláusula USING protege o SELECT. A cláusula WITH CHECK protege o INSERT/UPDATE.
CREATE POLICY tenant_isolation_policy ON products
    USING (tenant_id = current_setting('app.current_tenant'))
    WITH CHECK (tenant_id = current_setting('app.current_tenant'));

-- Inserir dados de teste para diferentes Tenants
INSERT INTO products (name, price, tenant_id) VALUES ('Iphone 15 (Tenant A)', 5000, 'TENANT_A');
INSERT INTO products (name, price, tenant_id) VALUES ('Macbook M3 (Tenant A)', 12000, 'TENANT_A');
INSERT INTO products (name, price, tenant_id) VALUES ('Galaxy S24 (Tenant B)', 4500, 'TENANT_B');
