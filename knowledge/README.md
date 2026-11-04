# Knowledge Base

## What This Is

This knowledge base is **HRPAuth-Spigot's memo to itself**. It serves
self-hosting and maintenance of this repository: what the plugin actually is,
which backend endpoints it calls, and where the backend side is not yet real.
It is maintained alongside the code and is not meant to make external projects
accommodate this one.

## Order of Authority

This knowledge base **does not define outward behavior**. Where the behavior of
this plugin touches the HA ecosystem's external contracts (notably the
`/admin/*` endpoints it calls), the authoritative definitions live in
[HA-Contract](https://github.com/CoreMatch/HA-Contract) (OpenAPI specs under
`docs/api/openapi/`). If an entry here ever appears to contradict a contract,
**the contract wins and the entry is stale — fix the entry, never the
contract**, and surface the conflict to the owner.

## Format

Each entry is one page in ADR style:

- **Status** (`accepted` / `superseded` / ...), **Date**
- **Context** — the problem and constraints
- **Decision** — what was chosen
- **Consequences** — including costs and limitations
- **Evidence** — `file:line`, protocol entries, commit hashes. Every piece of
  evidence must have been actually checked; anything that could not be verified
  is marked "not verified". Do not fabricate.

## Entries

| # | Title | Status | Date |
|---|-------|--------|------|
| [0001](0001-velocity-proxy-plugin-despite-repo-name.md) | Velocity Proxy Plugin Despite the Repository Name | accepted | 2026-11-04 |
| [0002](0002-admin-endpoints-called-but-not-implemented.md) | `/admin/*` Endpoints Called but Not Implemented in the Backend | accepted (historical; backend gap closed 2026-11-05, see entry update) | 2026-11-04 |
| [0003](0003-service-auth-via-oauth2-client-credentials.md) | Service Auth via OAuth2 client_credentials (Super Client) | accepted | 2026-11-04 |
