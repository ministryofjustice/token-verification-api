package uk.gov.justice.digital.hmpps.tokenverification.resource

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.tokenverification.service.JwtAuthHelper
import java.time.Duration

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTest {
  @Autowired
  internal lateinit var webTestClient: WebTestClient

  @Autowired
  internal lateinit var jwtHelper: JwtAuthHelper

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  internal fun setAuthorisation(
    user: String = "token-verification-api-client",
    roles: List<String> = listOf()
  ): (HttpHeaders) -> Unit {
    val token = jwtHelper.createJwt(
      subject = user,
      scope = listOf("read", "write"),
      expiryTime = Duration.ofHours(1L),
      roles = roles
    )
    return { it.set(HttpHeaders.AUTHORIZATION, "Bearer $token") }
  }
}
