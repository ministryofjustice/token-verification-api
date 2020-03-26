package uk.gov.justice.digital.hmpps.tokenverification.resource

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.test.context.ActiveProfiles
import redis.embedded.RedisServer


@ActiveProfiles("test")
abstract class RedisTest {
  companion object {
    @JvmField
    val redisServer: RedisServer = RedisServer(6379)

    @BeforeAll
    @JvmStatic
    fun startRedis() {
      redisServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopRedis() {
      redisServer.stop()
    }
  }
}
