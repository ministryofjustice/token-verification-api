# Scripts

Local dev/debug utility scripts for `token-verification-api`. These are not part of the build or CI — they exist purely to help with development.

## `dump-tokens.sh`

Dumps every `Token` record currently stored in Redis, as pretty-printed JSON — including each token's remaining TTL and computed expiry timestamp.

### Background

`token-verification-api` stores active tokens in Redis as `Token` hashes (see [TokenRepository.kt](../src/main/kotlin/uk/gov/justice/digital/hmpps/tokenverification/data/TokenRepository.kt)), each with a 24-hour TTL (`@RedisHash(timeToLive = 86400)`). Each hash contains:

- `jwtId` — the JWT ID (`jti`) of the stored token, also used as the Redis key suffix (`token:<jwtId>`)
- `authJwtId` — the JWT ID of the auth code/session token associated with this entry
- `subject` — the JWT `sub` claim (typically the username)
- `_class` — Spring Data Redis's internal type marker

There's no built-in UI for browsing this data, so this script uses `redis-cli` directly to list and format it.

### Requirements

- `redis-cli` on your `PATH` (`brew install redis`)
- `jq` on your `PATH` (`brew install jq`)
- A reachable Redis instance (defaults to `localhost:6379`)

### Usage

```bash
./scripts/dump-tokens.sh [redis-port]
```

- `redis-port` (optional) — the port your local Redis is listening on. Defaults to `6379` if omitted.

Examples:

```bash
# Use the default port (6379)
./scripts/dump-tokens.sh

# Use a different port, e.g. if Redis is mapped to 6380
./scripts/dump-tokens.sh 6380
```

### Example output

```json
{
  "key": "token:103e6b89-c096-49c3-89e3-2d05a6ee8bec",
  "ttlSeconds": 16855,
  "expiresAt": "2026-08-18T14:17:10Z",
  "value": {
    "_class": "uk.gov.justice.digital.hmpps.tokenverification.data.Token",
    "authJwtId": "551e8c00-a3c5-4214-b116-c59409dc2c85",
    "jwtId": "103e6b89-c096-49c3-89e3-2d05a6ee8bec",
    "subject": "PGRIME_GEN"
  }
}
```

- `ttlSeconds` — seconds remaining before Redis automatically expires this key (from `TTL <key>`)
- `expiresAt` — the TTL translated into an explicit ISO 8601 UTC timestamp, for readability

### Notes

- Only real `Token` hash keys (`token:<uuid>`) are shown — Spring Data Redis's secondary index keys (e.g. `token:subject:*`, `token:authJwtId:*`, which are Redis Sets, not Hashes) are filtered out.
- `expiresAt` is computed locally from the current time (`date -u`) plus the TTL returned by Redis — it is not stored in Redis itself, since Redis only tracks TTL as a relative countdown, not an absolute timestamp.
