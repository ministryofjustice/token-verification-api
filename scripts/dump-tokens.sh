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
  if ! echo "$key" | grep -qE '^token:[0-9a-f-]+$'; then
    echo "Warning: token hash key does not look like a UUID: $key" >&2
  fi

  ttl_seconds="$(redis-cli -p "$PORT" ttl "$key")"
  # Compute an explicit expiry timestamp (ISO 8601, UTC) from the current time + TTL.
  expires_at="$(date -u -v+"${ttl_seconds}"S +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -d "+${ttl_seconds} seconds" +"%Y-%m-%dT%H:%M:%SZ")"
  redis-cli -p "$PORT" hgetall "$key" | paste - - | jq -Rn --arg key "$key" --arg ttl "$ttl_seconds" --arg expiresAt "$expires_at" '
    { key: $key, ttlSeconds: ($ttl | tonumber), expiresAt: $expiresAt, value: ([inputs | split("\t") | {(.[0]): .[1]}] | add) }
  '
done
