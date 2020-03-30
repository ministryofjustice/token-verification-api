package uk.gov.justice.digital.hmpps.tokenverification.resource

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.tokenverification.data.Token
import uk.gov.justice.digital.hmpps.tokenverification.data.TokenRepository
import uk.gov.justice.digital.hmpps.tokenverification.service.JwtAuthHelper
import uk.gov.justice.digital.hmpps.tokenverification.service.JwtAuthHelper.JwtParameters
import java.util.*

@Suppress("ClassName")
class TokenResourceTest : IntegrationTest() {
  @MockBean
  private lateinit var tokenRepository: TokenRepository

  private val jwtHelper = JwtAuthHelper()

  @Nested
  inner class verifyToken {
    @Test
    fun `verify token`() {
      val jwt = jwtHelper.createJwt(JwtParameters(subject = "bob", jwtId = "jwt id"))
      val token = Token("JWT ID", "Auth id", "bob")

      whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(token))

      webTestClient.post().uri("/token/verify")
          .bodyValue(jwt)
          .exchange()
          .expectStatus().isOk
          .expectBody().json("{ active: true }")
    }

    @Test
    fun `verify token parse exception`() {
      webTestClient.post().uri("/token/verify")
          .bodyValue("not a jwt")
          .exchange()
          .expectStatus().isBadRequest
          .expectBody().json("verify_token_badrequest_parse_exception".loadJson())
    }

    @Test
    fun `verify token validation exception`() {
      val jwt = jwtHelper.createJwt(JwtParameters(subject = "", jwtId = "jwt id"))
      webTestClient.post().uri("/token/verify")
          .bodyValue(jwt)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody().json("verify_token_badrequest_validation_exception".loadJson())
    }

    @Test
    fun `verify token not found`() {
      val jwt = jwtHelper.createJwt(JwtParameters(subject = "subj", jwtId = "jwt id"))
      webTestClient.post().uri("/token/verify")
          .bodyValue(jwt)
          .exchange()
          .expectStatus().isOk
          .expectBody().json("{ active: true }")
    }
  }

  @Nested
  inner class addToken {
    @Test
    fun `add token`() {
      val jwt = jwtHelper.createJwt(JwtParameters(subject = "bob", jwtId = "jwt id"))
      webTestClient.post().uri("/token/auth_id")
          .bodyValue(jwt)
          .exchange()
          .expectStatus().isOk

      verify(tokenRepository).save(Token("jwt id", "auth_id", "bob"))
    }
  }

  @Nested
  inner class addRefreshToken {
    @Test
    fun `add refresh token`() {
      whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(Token("access id", "auth id", "subj")))

      val jwt = jwtHelper.createJwt(JwtParameters(subject = "bob", jwtId = "jwt id"))
      webTestClient.post().uri("/token/refresh/access_jwt_id")
          .bodyValue(jwt)
          .exchange()
          .expectStatus().isOk

      verify(tokenRepository).save(Token("jwt id", "auth_id", "bob"))
    }
  }

  @Nested
  inner class revokeTokens {
    @Test
    fun `revoke token`() {
      val token = Token("access id", "auth id", "subj")
      whenever(tokenRepository.findByAuthJwtId(anyString())).thenReturn(listOf(
          token))

      webTestClient.delete().uri("/token/auth_jwt_id")
          .exchange()
          .expectStatus().isOk

      verify(tokenRepository).delete(token)
    }
  }

  private fun String.loadJson(): String =
      TokenResourceTest::class.java.getResource("$this.json").readText()
}
