package uk.gov.justice.digital.hmpps.tokenverification.resource

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiDocsTest {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Test
  fun `open api docs are available`() {
    webTestClient.get()
      .uri("/webjars/swagger-ui/index.html?configUrl=/v3/api-docs")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `open api docs redirect to correct page`() {
    webTestClient.get()
      .uri("/swagger-ui.html")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().is3xxRedirection
      .expectHeader().value("Location") { it.contains("/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config") }
  }

  @Test
  fun `the swagger json is valid`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("messages").doesNotExist()
  }

  @Test
  fun `the swagger json contains the version number`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("info.version").isEqualTo(ISO_DATE.format(LocalDate.now()))
  }
}
