package nexus.authz

import rego.v1

# Decisão padrão é negar tudo
default allow := false

# Permitir se:
allow if {
    # O usuário pertence ao Tenant correto (Isolamento)
    input.user.tenant_id == input.resource.tenant_id

    # O método HTTP é permitido para a Role do usuário
    user_has_permission
}

# Verificação de Permissões (RBAC)
user_has_permission if {
    input.user.role == "ADMIN"
}

user_has_permission if {
    input.user.role == "USER"
    input.request.method == "GET" # User só lê
}

# Detalhes da negação (útil para auditoria)
reason := "Acesso negado: Inconsistência de Tenant ou Horário não permitido." if {
    not allow
}
