# ADR-0003: Service Auth via OAuth2 client_credentials (Super Client)

- **Status**: accepted
- **Date**: 2026-11-04

## Context

The plugin must authenticate itself to HRPAuth to perform privileged
operations. Early versions used a static service-token config value. The team
moved to a proper OAuth2 flow so tokens are short-lived and issued by the core.

## Decision

The plugin obtains a service token via the **OAuth2 `client_credentials`
grant**, using HRPAuth's built-in super client:

- `OAuthClient` POSTs to `{url}/oauth/token` with form body
  `grant_type=client_credentials&client_id=...&client_secret=...`, caches the
  token (expiring it 30s early), and parses `access_token`/`expires_in`.
- The `client_id`/`client_secret` come from plugin config and correspond to
  HRPAuth's `oauth2.super_client_id`/`super_client_secret` (the built-in super
  client, which allows only `client_credentials`).
- The plugin does **not** call HRPAuth's `/services/*` inter-service endpoints.
  Its only outbound URLs are `/oauth/token`, `/admin/claim-user`, and
  `/admin/force-bind`.
  **Update 2026-11-05**: since commit `e8067dd` the two admin URLs carry the
  `/yggdrasil-api` relay prefix (`{url}/yggdrasil-api/admin/claim-user`,
  `{url}/yggdrasil-api/admin/force-bind`); the HRPAuth core relay strips the
  prefix and forwards to HRPAuth-Yggdrasil-API. See entry
  [0002](0002-admin-endpoints-called-but-not-implemented.md)'s update.

## Consequences

- A hard-coded `scope=user.claim.as-service` parameter was added once and then
  removed; the current HEAD sends **no scope parameter**, so the core applies
  its default scope handling for the super client.
- Several config sections (`presence`, `log`, `verify-tls`) are present but
  have no runtime consumer — reserved/dead config, with `presence` documented
  as optional.
- Cost: the plugin depends on the super client secret being correctly
  configured; token acquisition failure blocks `reg`/`forcebind`.

## Evidence

- Token acquisition: `src/main/java/com/hrp/auth/proxy/OAuthClient.java:40-50`
  (POST `/oauth/token`, form body `:41-43`), caching `:33-38`, parsing
  `:58-60`; URL at `:46`.
- Switch to client credentials: commit `7d1e08f` ("switch registration auth to
  oauth2 client credentials flow"); scope added in `51ae66b`
  (`&scope=user.claim.as-service`) and removed in `0394469` ("remove hardcoded
  scope parameter").
- Super client linkage: plugin config credentials
  `src/main/java/com/hrp/auth/proxy/config/Config.java:172-173` (comment
  pointing to core `oauth2.super_client_id/secret`); core super client
  HRPAuth `/home/lnb/Desktop/HA/HRPAuth/services/oauth2_service.go::upsertBuiltInSuperClient`;
  core grant handling HRPAuth
  `/home/lnb/Desktop/HA/HRPAuth/controllers/oauth2_controller.go::Token` →
  `/home/lnb/Desktop/HA/HRPAuth/services/oauth2_service.go::IssueClientCredentialsToken`;
  auth level for service tokens HRPAuth
  `/home/lnb/Desktop/HA/HRPAuth/controllers/auth_level.go::resolveAuthLevel`.
- No `/services/*` calls from the plugin: grep `URI.create` yields exactly
  `src/main/java/com/hrp/auth/proxy/OAuthClient.java:46`,
  `src/main/java/com/hrp/auth/proxy/RegisterCommand.java:75`,
  `src/main/java/com/hrp/auth/proxy/ForceBindCommand.java:73`.
- Dead config: `src/main/java/com/hrp/auth/proxy/config/Config.java:49-59,121-127`
  (presence section), `getPresence()`
  `src/main/java/com/hrp/auth/proxy/config/Config.java::getPresence` has no
  callers.
- Commits: `7d1e08f`, `51ae66b`, `0394469`, `7710fd5` (earlier static
  service-token config).
- Not verified: the core's exact behavior for an empty scope in
  `IssueClientCredentialsToken`
  (`/home/lnb/Desktop/HA/HRPAuth/services/oauth2_service.go::IssueClientCredentialsToken`
  onward not fully traced); whether the dead config sections are planned
  features.
