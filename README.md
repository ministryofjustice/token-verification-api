# token-verification-api

[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Ftoken-verification-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#token-verification-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/token-verification-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/token-verification-api)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/token-verification-api/status)](https://quay.io/repository/hmpps/token-verification-api)
[![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://token-verification-api-dev.prison.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

Spring Boot JSON API to control verification of access and refresh tokens of prisoners for HMPPS.
 
### Building

```bash
./gradlew build
```

### Running

```bash
./gradlew bootRun
```

### Health

- `/health/ping`: will respond with `{"status":"UP"}` to all requests.  This should be used by dependent systems to check connectivity to token-verification-api,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by token-verification-api health monitoring (e.g. pager duty) and not other systems who wish to find out the state of token-verification-api.
- `/info`: provides information about the version of deployed application.

### Pre Release Testing

Token verification api is best tested by interaction with auth (https://sign-in-preprod.hmpps.service.justice.gov.uk/auth/).  To manually smoke test / regression test token verification api prior to release:

1. Login to auth
1. Navigate to whereabouts
1. In separate tab navigate directly to auth and logout
1. In previous tab perform action - should be directed back to auth as not logged in (only if enabled)

All the above events will generate calls to token verification and will fail if token verification is not working correctly.  The last item will only succeed if token verification is enabled for that application in that environment.

#### Redis

We are using [Redis repositories](https://docs.spring.io/spring-data/data-redis/docs/current/reference/html/#redis.repositories) 
from Spring Boot to store the tokens in Redis.

Each token record consists of a `jwtId`, `authJwtId` and `subject`.  The `jwtId` is the `jti` in an individual access
or refresh token granted to a client.  The `authJwtId` is the owning session in auth from which the access token or
refresh token was granted, or the `jwtId` for client credentials if there is no owning session.  The `subject` is the
username, or where that isn't set (e.g. for client credentials), the client id of the grant.  Both the `authJwtId` and
 `subject` are indexed in Redis, which means that there are Redis sets for each stored separately too.

If we insert a token record for:
```
    jwtId: a5b0dd32-2763-4577-bce8-7339c8bd4bd2
    authJwtId: c70857f7-314e-4e21-a52f-34f995d465ff
    subject: ITAG_USER
```
 into Redis we will therefore get:
1. a Redis hash at `token:a5b0dd32-2763-4577-bce8-7339c8bd4bd2` with contents:
    ```
    1) "_class"
    2) "uk.gov.justice.digital.hmpps.tokenverification.data.Token"
    3) "jwtId"
    4) "a5b0dd32-2763-4577-bce8-7339c8bd4bd2"
    5) "authJwtId"
    6) "c70857f7-314e-4e21-a52f-34f995d465ff"
    7) "subject"
    8) "ITAG_USER"
    ```
1. An index Redis set at `token:a5b0dd32-2763-4577-bce8-7339c8bd4bd2:idx` linking the token to the auth JWT and 
    subject, with members:
    ```
    1) "token:authJwtId:c70857f7-314e-4e21-a52f-34f995d465ff"
    2) "token:subject:ITAG_USER"
    ```
1. A phantom Redis hash record at `token:a5b0dd32-2763-4577-bce8-7339c8bd4bd2:phantom` with same contents as the Redis
 hash at 1, but with a 5 minute later expiry.  This is so that Spring can clean up the records on expiry. 
1. An index Redis set for all tokens granted from the auth owning session at
 `token:authJwtId:c70857f7-314e-4e21-a52f-34f995d465ff` with initial members:
    ```
    1) "a5b0dd32-2763-4577-bce8-7339c8bd4bd2"
    
    ```
1. An index Redis set for all tokens granted for the subject at `token:subject:ITAG_USER` with initial members:
    ```
    1) "a5b0dd32-2763-4577-bce8-7339c8bd4bd2"
    ```


##### Connecting to a Redis Instance

Tokens are stored in a redis instance in AWS.  Follow [DPS Runbook](https://dsdmoj.atlassian.net/wiki/spaces/NOM/pages/1739325587/DPS+Runbook#Connecting-to-elasticache-to-view-existing-sessions)
to connect to the redis store in a cloud platform environment.

