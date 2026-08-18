#!/usr/bin/env bash
# Dumps all Token hashes stored in Redis by token-verification-api, as pretty JSON.
#
# Usage: ./dump-tokens.sh [redis-port]
# Defaults to port 6379 if not specified.

set -euo pipefail

PORT="${1:-6379}"

redis-cli -p "$PORT" keys 'token:*' | grep -E '^token:[a-f0-9-]+$' | while read -r key; do
  ttl_seconds="$(redis-cli -p "$PORT" ttl "$key")"
  # Compute an explicit expiry timestamp (ISO 8601, UTC) from the current time + TTL.
  expires_at="$(date -u -v+"${ttl_seconds}"S +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -d "+${ttl_seconds} seconds" +"%Y-%m-%dT%H:%M:%SZ")"
  redis-cli -p "$PORT" hgetall "$key" | paste - - | jq -Rn --arg key "$key" --arg ttl "$ttl_seconds" --arg expiresAt "$expires_at" '
    { key: $key, ttlSeconds: ($ttl | tonumber), expiresAt: $expiresAt, value: ([inputs | split("\t") | {(.[0]): .[1]}] | add) }
  '
done
