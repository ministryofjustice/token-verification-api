package uk.gov.justice.digital.hmpps.tokenverification.resource

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import redis.embedded.RedisServer


@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTest {
  @Suppress("unused")
  @Autowired
  lateinit var webTestClient: WebTestClient

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

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
