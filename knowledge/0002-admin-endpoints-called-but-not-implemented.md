# ADR-0002: `/admin/*` Endpoints Called but Not Implemented in the Backend

- **Status**: accepted
- **Date**: 2026-11-04

## Context

The plugin's `reg` and `forcebind` commands need privileged backend operations:
claiming a proxy-registered account and force-binding a Mojang UUID to another
account. These are modeled as `/admin/*` endpoints on the HRPAuth backend. The
plugin was built against those endpoints before the backend handlers existed.

## Decision

The plugin **calls two `/admin/*` endpoints** on HRPAuth, but **the HRPAuth
backend does not implement them**. This is a known, tracked gap, not a bug in
the plugin:

- `reg` → `POST {url}/admin/claim-user` with body
  `{"username","email","password"}` and `Authorization: Bearer <serviceToken>`.
- `forcebind` → `POST {url}/admin/force-bind` with a body that includes
  `manage_token`.

## Consequences

- Both commands will receive `404 route_not_found` from a current HRPAuth core,
  because no `/admin` route group exists and unmatched requests fall through to
  the NoRoute handler.
- The authoritative request/response shapes live in HA-Contract's Yggdrasil
  business OpenAPI spec, and HA-Contract's roadmap lists the backend handler as
  **Pending**. The plugin side is considered done; the backend side is not.
- Cost: the `reg`/`forcebind` features are effectively non-functional until the
  backend handlers land. Anyone testing them should expect 404, not a logic
  error.

## Evidence

- Plugin call sites (the only two `/admin` references in the plugin):
  `src/main/java/com/hrp/auth/proxy/RegisterCommand.java:74`
  (`/admin/claim-user`), body `:69-71`, bearer header `:76`, javadoc `:19-21`.
  `src/main/java/com/hrp/auth/proxy/ForceBindCommand.java:79`
  (`/admin/force-bind`), `manage_token` body `:74-76`, javadoc `:19-22`.
- Backend has no `/admin` routes: HRPAuth `main.go:174-244` is the full route
  table with no `/admin` group; grep for `claim-user|force-bind|ClaimUser|
  ForceBind` across HRPAuth `*.go` returns zero matches; unmatched routes hit
  NoRoute → 404 `route_not_found` (HRPAuth `main.go:246-256`).
- Contract defines the endpoints: HA-Contract
  `docs/api/openapi/hrpauth-yggdrasil-business.yaml:12` (`/admin/claim-user`)
  and `:20` (`/admin/force-bind`).
- Roadmap marks backend as pending: HA-Contract `agents.md:60` (roadmap item
  2.1, "HA backend handler not yet implemented", status Pending).
- Register-command payload history: `2d9579d` (add username) → `a434801`
  (remove username) → `f5eeac6` (restore username, add error mapping
  `RegisterCommand.java:89-99`).
- Not verified: the field-by-field request/response schemas in the OpenAPI spec
  were not exhaustively compared to the plugin's JSON bodies.
