package nexus.authz_test

import rego.v1
import data.nexus.authz

# Teste: Bloquear se o usuário tentar acessar recursos de outro Tenant
test_deny_cross_tenant if {
    not authz.allow with input as {
        "user": {"tenant_id": "TENANT_A", "role": "ADMIN"},
        "resource": {"tenant_id": "TENANT_B"}, # Tentando invadir!
        "request": {"method": "GET", "path": "/api/products"}
    }
}

# Teste: Permitir acesso se o Tenant for o mesmo
test_allow_same_tenant if {
    authz.allow with input as {
        "user": {"tenant_id": "TENANT_A", "role": "ADMIN"},
        "resource": {"tenant_id": "TENANT_A"},
        "request": {"method": "GET", "path": "/api/products"}
    }
}
