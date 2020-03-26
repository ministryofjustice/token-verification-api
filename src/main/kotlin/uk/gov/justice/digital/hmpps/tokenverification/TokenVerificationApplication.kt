package uk.gov.justice.digital.hmpps.tokenverification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories


@SpringBootApplication
class TokenVerificationApplication

fun main(args: Array<String>) {
  runApplication<TokenVerificationApplication>(*args)
}

@Configuration
@EnableRedisRepositories
class AppConfig
