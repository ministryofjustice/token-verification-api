package uk.gov.justice.digital.hmpps.tokenverification.resource

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.tokenverification.data.Token
import uk.gov.justice.digital.hmpps.tokenverification.data.TokenRepository
import java.util.*

class TokenResourceTest : IntegrationTest() {
  @MockBean
  private lateinit var tokenRepository: TokenRepository

  @Test
  fun `verify token no authority`() {
    val jwt = jwtHelper.createJwt(subject = "bob")

    webTestClient.post().uri("/token/verify")
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isUnauthorized
  }

  @Test
  fun `verify token`() {
    val jwt = jwtHelper.createJwt(subject = "bob")
    val token = Token("JWT ID", "Auth id", "bob")

    whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(token))

    webTestClient.post().uri("/token/verify")
        .headers(setAuthorisation())
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isOk
        .expectBody().json("{ active: true }")
  }

  @Test
  fun `verify token parse exception`() {
    webTestClient.post().uri("/token/verify")
        .headers(setAuthorisation())
        .bodyValue("not a jwt")
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().json("verify_token_badrequest_parse_exception".loadJson())
  }

  @Test
  fun `verify token validation exception`() {
    val jwt = jwtHelper.createJwt(subject = "")
    webTestClient.post().uri("/token/verify")
        .headers(setAuthorisation())
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().json("verify_token_badrequest_validation_exception".loadJson())
  }

  @Test
  fun `verify token not found`() {
    val jwt = jwtHelper.createJwt(subject = "subj")
    webTestClient.post().uri("/token/verify")
        .headers(setAuthorisation())
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isOk
        .expectBody().json("{ active: false }")
  }

  @Test
  fun `add token`() {
    val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "jwt id")
    webTestClient.post().uri("/token/auth_id")
        .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isOk

    verify(tokenRepository).save(Token("jwt id", "auth_id", "bob"))
  }

  @Test
  fun `add token incorrect role`() {
    val jwt = jwtHelper.createJwt(subject = "bob")
    webTestClient.post().uri("/token/auth_id")
        .headers(setAuthorisation(roles = listOf("ROLE_INCORRECT")))
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isForbidden

    verifyZeroInteractions(tokenRepository)
  }

  @Test
  fun `add refresh token`() {
    whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(Token("access id", "auth id", "subj")))

    val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "jwt id")
    webTestClient.post().uri("/token/refresh/access_jwt_id")
        .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isOk

    verify(tokenRepository).save(Token("jwt id", "auth id", "bob"))
  }

  @Test
  fun `add refresh token incorrect role`() {
    val jwt = jwtHelper.createJwt(subject = "bob")
    webTestClient.post().uri("/token/refresh/access_jwt_id")
        .headers(setAuthorisation(roles = listOf("ROLE_INCORRECT")))
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isForbidden

    verifyZeroInteractions(tokenRepository)
  }

  @Test
  fun `revoke token`() {
    val token = Token("access id", "auth id", "subj")
    whenever(tokenRepository.findByAuthJwtId(anyString())).thenReturn(listOf(
        token))

    webTestClient.delete().uri("/token/auth_jwt_id")
        .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
        .exchange()
        .expectStatus().isOk

    verify(tokenRepository).delete(token)
  }

  @Test
  fun `revoke token incorrect role`() {
    webTestClient.delete().uri("/token/auth_jwt_id")
        .headers(setAuthorisation(roles = listOf("ROLE_INCORRECT")))
        .exchange()
        .expectStatus().isForbidden

    verifyZeroInteractions(tokenRepository)
  }

  private fun String.loadJson(): String =
      TokenResourceTest::class.java.getResource("$this.json").readText()
}
