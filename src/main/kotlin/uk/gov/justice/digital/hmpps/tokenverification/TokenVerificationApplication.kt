package uk.gov.justice.digital.hmpps.tokenverification

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories


@SpringBootApplication
class TokenVerificationApplication

fun main(args: Array<String>) {
  runApplication<TokenVerificationApplication>(*args)
}

@Configuration
@EnableRedisRepositories
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
