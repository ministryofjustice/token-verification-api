#!/usr/bin/env bash
# Dumps all Token hashes stored in Redis by token-verification-api, as pretty JSON.
#
# Usage: ./dump-tokens.sh [redis-port]
# Defaults to port 6379 if not specified.

set -euo pipefail

PORT="${1:-6379}"

redis-cli -p "$PORT" --scan --pattern 'token:*' | while read -r key; do
  # Skip Spring Data Redis's internal ":phantom" hashes (used to support expiry
  # events) - only real Token records (plain "token:<id>" keys) should be shown.
  case "$key" in
    *:phantom) continue ;;
  esac

  key_type="$(redis-cli -p "$PORT" type "$key")"
  [ "$key_type" = "hash" ] || continue

  # jwtId/authJwtId are arbitrary Strings in the app (not enforced as UUIDs), so
  # this check is informational only - it does not filter out non-UUID keys.
  if ! echo "$key" | grep -qiE '^token:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'; then
    echo "Warning: token hash key does not look like a UUID: $key" >&2
  fi

  ttl_seconds="$(redis-cli -p "$PORT" ttl "$key")"

  # TTL of -2 means the key no longer exists (e.g. it expired between the
  # --scan and this call) - skip it rather than reporting a bogus expiry.
  if [ "$ttl_seconds" -eq -2 ]; then
    continue
  fi

  # TTL of -1 means the key exists but has no expiry set. Only compute an
  # explicit expiry timestamp (ISO 8601, UTC) when there's a real, positive TTL.
  if [ "$ttl_seconds" -ge 0 ]; then
    expires_at="$(date -u -v+"${ttl_seconds}"S +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -d "+${ttl_seconds} seconds" +"%Y-%m-%dT%H:%M:%SZ")"
    expires_at_json_arg=(--arg expiresAt "$expires_at")
  else
    expires_at_json_arg=(--argjson expiresAt null)
  fi

  redis-cli -p "$PORT" hgetall "$key" | paste - - | jq -Rn --arg key "$key" --arg ttl "$ttl_seconds" "${expires_at_json_arg[@]}" '
    { key: $key, ttlSeconds: ($ttl | tonumber), expiresAt: $expiresAt, value: ([inputs | split("\t") | {(.[0]): .[1]}] | add) }
  '
done
