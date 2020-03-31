package uk.gov.justice.digital.hmpps.tokenverification

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories


@SpringBootApplication
class TokenVerificationApplication

fun main(args: Array<String>) {
  runApplication<TokenVerificationApplication>(*args)
}

// we want keyspace notifications, but have to empty the config parameter (default Ex) since elasticache doesn't support
// changing the config.  If we move off elasticache then need to remove the config parameter and let it use the default.
@Configuration
@EnableRedisRepositories(
    enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP,
    keyspaceNotificationsConfigParameter = "\${application.keyspace-notifications:}")
class AppConfig

@Configuration
class VersionLogger(val buildProperties: BuildProperties) {
  @EventListener(ApplicationReadyEvent::class)
  fun logVersionOnStartup() {
    log.info("Version {} started", buildProperties.version)
  }

  companion object {
    private val log = LoggerFactory.getLogger(VersionLogger::class.java)
  }
}
