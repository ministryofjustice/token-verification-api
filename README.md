# token-verification-api

[![CircleCI](https://circleci.com/gh/ministryofjustice/token-verification-api/tree/master.svg?style=svg)](https://circleci.com/gh/ministryofjustice/token-verification-api)
[![Known Vulnerabilities](https://snyk.io/test/github/ministryofjustice/token-verification-api/badge.svg)](https://snyk.io/test/github/ministryofjustice/token-verification-api)

Self-contained fat-jar micro-service to control verification of access and refresh tokens
 
### Building

```bash
./gradlew build
```

### Running

```bash
./gradlew bootRun
```

#### Health

- `/health/ping`: will respond with status `UP` to all requests.  This should be used by dependent systems to check connectivity to token-verification-api,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by token-verification-api health monitoring (e.g. pager duty) and not other systems who wish to find out the state of token-verification-api.
- `/info`: provides information about the version of deployed application.

