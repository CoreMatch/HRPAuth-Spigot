# ADR-0002: `/admin/*` Endpoint Calls Relayed to HRPAuth-Yggdrasil-API

- **Status**: accepted
- **Date**: 2026-11-04

## Context

The plugin's `reg` and `forcebind` commands need privileged backend
operations: claiming a proxy-registered account and force-binding a Mojang
UUID to another account. These are modeled as `/admin/*` endpoints, specified
in HA-Contract's Yggdrasil business OpenAPI spec and implemented by the
HRPAuth-Yggdrasil-API microservice. Anyone deploying or debugging the two
commands needs to know how the calls reach that service.

## Decision

The plugin **calls two `/admin/*` endpoints through the HRPAuth core relay
prefix**:

- `reg` → `POST {url}/yggdrasil-api/admin/claim-user` with body
  `{"username","email","password"}` and `Authorization: Bearer <serviceToken>`.
- `forcebind` → `POST {url}/yggdrasil-api/admin/force-bind` with the same
  body shape.

The HRPAuth core relay matches the `/yggdrasil-api` prefix, strips it, and
forwards the request to HRPAuth-Yggdrasil-API, which implements both
handlers. Both calls authenticate with the same OAuth2 service token (see
entry [0003](0003-service-auth-via-oauth2-client-credentials.md));
force-bind is authorized by the `user.force-bind.as-service` scope, the same
service-token tier as claim-user.

## Consequences

- The authoritative request/response shapes live in HA-Contract's Yggdrasil
  business OpenAPI spec (paths `/admin/claim-user` and `/admin/force-bind`);
  HA-Contract's roadmap item 2.1 lists the backend handlers as implemented.
- Cost: the calls depend on the core relay rule for `/yggdrasil-api`; a
  relay forwarding failure returns a 502 relay error from the core rather
  than a plugin-side error.

## Evidence

- Plugin call sites (the only two `/admin` references in the plugin):
  `src/main/java/com/hrp/auth/proxy/RegisterCommand.java:75`
  (`/yggdrasil-api/admin/claim-user`), body `:70-72`, bearer header `:77`,
  javadoc `:19-22`.
  `src/main/java/com/hrp/auth/proxy/ForceBindCommand.java:73`
  (`/yggdrasil-api/admin/force-bind`), body `:68-70`, javadoc `:19-22`.
- Backend handlers: HRPAuth-Yggdrasil-API
  [xref] `/home/lnb/Desktop/HA/HRPAuth-Yggdrasil-API/main.go:L163-L166`
  (`/admin` route group),
  [xref] `/home/lnb/Desktop/HA/HRPAuth-Yggdrasil-API/controllers/admin_controller.go::ClaimUser`
  (scope check `user.claim.as-service`,
  [xref] `/home/lnb/Desktop/HA/HRPAuth-Yggdrasil-API/controllers/admin_controller.go:L67`)
  and
  [xref] `/home/lnb/Desktop/HA/HRPAuth-Yggdrasil-API/controllers/admin_controller.go::ForceBind`
  (scope check `user.force-bind.as-service`,
  [xref] `/home/lnb/Desktop/HA/HRPAuth-Yggdrasil-API/controllers/admin_controller.go:L159`).
- Core relay prefix handling: HRPAuth
  [xref] `/home/lnb/Desktop/HA/HRPAuth/controllers/relay_controller.go::RelayMiddleware`
  (prefix match, strip, forward).
- Contract defines the endpoints: HA-Contract
  [xref] `/home/lnb/Desktop/HA/HA-Contract/docs/api/openapi/hrpauth-yggdrasil-business.yaml:L376`
  (`/admin/claim-user`) and
  [xref] `/home/lnb/Desktop/HA/HA-Contract/docs/api/openapi/hrpauth-yggdrasil-business.yaml:L422`
  (`/admin/force-bind`).
- Roadmap lists the backend handlers as implemented: HA-Contract
  [xref] `/home/lnb/Desktop/HA/HA-Contract/agents.md:L60` (roadmap item 2.1).
- Not verified: the field-by-field request/response schemas in the OpenAPI
  spec were not exhaustively compared to the plugin's JSON bodies.
