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

# Teste: Permitir USER lendo seu próprio tenant
test_allow_user_get if {
    authz.allow with input as {
        "user": {"tenant_id": "TENANT_B", "role": "USER"},
        "resource": {"tenant_id": "TENANT_B"},
        "request": {"method": "GET", "path": "/api/products"}
    }
}

# Teste: Negar USER tentando escrever (POST)
test_deny_user_cannot_write if {
    not authz.allow with input as {
        "user": {"tenant_id": "TENANT_A", "role": "USER"},
        "resource": {"tenant_id": "TENANT_A"},
        "request": {"method": "POST", "path": "/api/products"}
    }
}

# Teste: Negar USER tentando deletar (DELETE)
test_deny_user_cannot_delete if {
    not authz.allow with input as {
        "user": {"tenant_id": "TENANT_A", "role": "USER"},
        "resource": {"tenant_id": "TENANT_A"},
        "request": {"method": "DELETE", "path": "/api/products/1"}
    }
}

# Teste: Negar role desconhecida mesmo no tenant correto
test_deny_unknown_role if {
    not authz.allow with input as {
        "user": {"tenant_id": "TENANT_A", "role": "GUEST"},
        "resource": {"tenant_id": "TENANT_A"},
        "request": {"method": "GET", "path": "/api/products"}
    }
}

# Teste: Razão de negação está presente quando acesso negado
test_deny_reason_present if {
    authz.reason with input as {
        "user": {"tenant_id": "TENANT_A", "role": "USER"},
        "resource": {"tenant_id": "TENANT_B"},
        "request": {"method": "GET", "path": "/api/products"}
    }
}
