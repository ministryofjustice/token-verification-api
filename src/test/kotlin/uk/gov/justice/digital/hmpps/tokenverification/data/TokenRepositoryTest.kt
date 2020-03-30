package uk.gov.justice.digital.hmpps.tokenverification.data

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.tokenverification.resource.RedisExtension


@ExtendWith(RedisExtension::class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class TokenRepositoryTest {

  @Autowired
  lateinit var tokenRepository: TokenRepository

  @Test
  fun `should insert token`() {
    val token = Token("id", "auth id", "BOB")

    val id = tokenRepository.save(token).jwtId

    val savedToken = tokenRepository.findById(id).get()

    with(savedToken) {
      assertThat(jwtId).isEqualTo("id")
      assertThat(authJwtId).isEqualTo("auth id")
      assertThat(subject).isEqualTo("BOB")
    }
  }

  @Test
  fun `should search by auth id`() {
    val token = tokenRepository.save(Token("id", "auth id", "BOB"))
    val token2 = tokenRepository.save(Token("id2", "auth id", "BOB"))
    tokenRepository.save(Token("id3", "auth other", "OTHER"))

    val savedTokens = tokenRepository.findByAuthJwtId("auth id")
    assertThat(savedTokens).containsExactlyInAnyOrder(token, token2)
  }

  @Test
  fun `should search by username`() {
    val token = tokenRepository.save(Token("id", "auth id", "BOB"))
    val token2 = tokenRepository.save(Token("id2", "auth id2", "BOB"))
    tokenRepository.save(Token("id3", "auth id2", "OTHER"))

    val savedTokens = tokenRepository.findBySubject("BOB")
    assertThat(savedTokens).containsExactlyInAnyOrder(token, token2)
  }
}
