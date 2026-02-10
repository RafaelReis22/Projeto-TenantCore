# TenantCore — Architecture

## Overview

TenantCore is a multi-tenant security platform that enforces data isolation through Open Policy Agent (OPA) policy evaluation at the API gateway level.

## Services

| Service | Port | Description |
|---------|------|-------------|
| `tenantcore` | 9000 | API Gateway — routes requests, enforces tenant isolation via OPA |
| `product-service` | 9001 | Domain microservice — manages products per tenant |
| `postgres` | 5444 | Shared relational database with row-level tenant isolation |
| `opa` | 8181 | Policy engine — evaluates RBAC + ABAC authorization rules |
| `nexus-auth` | 8888 | Keycloak — identity provider and JWT issuer |
| `redis` | 6379 | Session/cache layer |

## Request Flow

```
Client
  │
  ▼
TenantCore Gateway (port 9000)
  │  extracts X-Tenant-ID + JWT
  ▼
OPA Engine (port 8181)
  │  evaluates: tenant match + role + working hours
  ├─ DENY → 403 Forbidden returned to client
  └─ ALLOW ──▶ Product Service (port 9001)
                  │
                  ▼
               PostgreSQL
               (filtered by tenant_id)
```

## Authorization Model

TenantCore uses **RBAC + ABAC** via OPA:

| Role | GET | POST | PUT | DELETE |
|------|-----|------|-----|--------|
| ADMIN | ✅ | ✅ | ✅ | ✅ |
| USER | ✅ | ❌ | ❌ | ❌ |
| (other) | ❌ | ❌ | ❌ | ❌ |

Additional constraint: access is only allowed between **08:00–20:00** (ABAC — time-based attribute).

## Multi-tenancy Strategy

- **Isolation unit**: `tenant_id` claim in the JWT token
- **Database**: Single schema, `tenant_id` column on all tables with Row-Level Security (see `postgres-init/01-init-rls.sql`)
- **Enforcement**: Gateway validates tenant consistency via OPA before forwarding the request
- **Identity**: Keycloak issues JWTs with tenant claims

## Key Design Decisions

### Why OPA?
OPA decouples authorization logic from business logic. Policies are stored as `.rego` files alongside the codebase, version-controlled, and testable with `opa test`.

### Why single database with RLS?
Simplifies operations at small-to-medium scale. PostgreSQL Row-Level Security provides a database-enforced fallback in addition to the application-layer enforcement.

### Why Keycloak?
Standards-based (OAuth 2.0 / OIDC), supports multi-tenant realm configuration, and integrates natively with Spring Security.

## Running Locally

```bash
docker compose up -d
```

Wait for all services to be healthy, then access:
- **TenantCore API**: http://localhost:9000
- **Product Service API**: http://localhost:9001
- **Keycloak Admin**: http://localhost:8888
- **OPA**: http://localhost:8181

## Running Policy Tests

```bash
opa test ./opa -v
```

## Running Unit Tests

```bash
# Product service
mvn -f services/product-service/pom.xml verify

# TenantCore gateway
mvn -f tenantcore/pom.xml verify
```
