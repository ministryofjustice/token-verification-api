package uk.gov.justice.digital.hmpps.tokenverification.services.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.tokenverification.resource.IntegrationTest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE

class InfoIntTest : IntegrationTest() {
  @Test
  fun `Info page contains git information`() {
    webTestClient.get().uri("/info")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("git.commit.id").isNotEmpty
  }

  @Test
  fun `Info page reports version`() {
    webTestClient.get().uri("/info")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("build.version").value<String> {
          assertThat(it).startsWith(LocalDateTime.now().format(ISO_DATE))
        }
  }
}
