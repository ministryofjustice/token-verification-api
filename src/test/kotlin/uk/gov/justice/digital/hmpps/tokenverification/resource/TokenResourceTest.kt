package uk.gov.justice.digital.hmpps.tokenverification.resource

import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.tokenverification.data.Token
import uk.gov.justice.digital.hmpps.tokenverification.data.TokenRepository
import java.util.*

class TokenResourceTest : IntegrationTest() {
  @MockitoBean
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
  fun `verify token using body`() {
    val jwt = jwtHelper.createJwt(subject = "bob")
    val token = Token("JWT ID", "Auth id", "bob")

    whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(token))

    webTestClient.post().uri("/token/verify")
      .headers(setAuthorisation())
      .bodyValue(jwt)
      .exchange()
      .expectStatus().isOk
      .expectBody().json("{ \"active\": true }")
  }

  @Test
  fun `verify token using header`() {
    val token = Token("JWT ID", "Auth id", "bob")

    whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(token))

    webTestClient.post().uri("/token/verify")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isOk
      .expectBody().json("{ \"active\": true }")
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
      .expectBody().json("{ \"active\": false }")
  }

  @Test
  fun `add token`() {
    val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "jwt id")
    webTestClient.post().uri { it.path("/token").queryParam("authJwtId", "auth_id").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
      .bodyValue(jwt)
      .exchange()
      .expectStatus().isOk

    verify(tokenRepository).save(Token("jwt id", "auth_id", "bob"))
  }

  @Test
  fun `add token with encoded slash`() {
    val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "jwt id")
    webTestClient.post().uri { it.path("/token").queryParam("authJwtId", "4QpGwPH2X/3KOAda3tlv/HjVHWo=").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
      .bodyValue(jwt)
      .exchange()
      .expectStatus().isOk

    verify(tokenRepository).save(Token("jwt id", "4QpGwPH2X/3KOAda3tlv/HjVHWo=", "bob"))
  }

  @Test
  fun `add token with plus sign`() {
    val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "jwt id")
    webTestClient.post().uri { it.path("/token").queryParam("authJwtId", "4+QpGwPH2X/3KOAda+3tlv/HjVHWo=").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
      .bodyValue(jwt)
      .exchange()
      .expectStatus().isOk

    verify(tokenRepository).save(Token("jwt id", "4+QpGwPH2X/3KOAda+3tlv/HjVHWo=", "bob"))
  }

  @Test
  fun `add token incorrect role`() {
    val jwt = jwtHelper.createJwt(subject = "bob")
    webTestClient.post().uri { it.path("/token").queryParam("authJwtId", "auth_id").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_INCORRECT")))
      .bodyValue(jwt)
      .exchange()
      .expectStatus().isForbidden

    verifyNoInteractions(tokenRepository)
  }

  @Test
  fun `add refresh token`() {
    whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(Token("access id", "auth id", "subj")))

    val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "jwt id")
    webTestClient.post().uri { it.path("/token/refresh").queryParam("accessJwtId", "access_jwt_id").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
      .bodyValue(jwt)
      .exchange()
      .expectStatus().isOk

    verify(tokenRepository).save(Token("jwt id", "auth id", "bob"))
  }

  @Test
  fun `add refresh token with plus sign`() {
    whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(Token("access id", "auth id", "subj")))

    val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "jwt id")
    webTestClient.post()
      .uri { it.path("/token/refresh").queryParam("accessJwtId", "4+QpGwPH2X/3KOAda+3tlv/HjVHWo=").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
      .bodyValue(jwt)
      .exchange()
      .expectStatus().isOk

    verify(tokenRepository).findById("4+QpGwPH2X/3KOAda+3tlv/HjVHWo=")
  }

  @Test
  fun `add refresh token incorrect role`() {
    val jwt = jwtHelper.createJwt(subject = "bob")
    webTestClient.post().uri { it.path("/token/refresh").queryParam("accessJwtId", "access_jwt_id").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_INCORRECT")))
      .bodyValue(jwt)
      .exchange()
      .expectStatus().isForbidden

    verifyNoInteractions(tokenRepository)
  }

  @Test
  fun `revoke token`() {
    val token = Token("access id", "auth id", "subj")
    whenever(tokenRepository.findByAuthJwtId(anyString())).thenReturn(
      listOf(
        token,
      ),
    )

    webTestClient.delete().uri { it.path("/token").queryParam("authJwtId", "auth_jwt_id").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
      .exchange()
      .expectStatus().isOk

    verify(tokenRepository).delete(token)
  }

  @Test
  fun `revoke token with plus sign`() {
    val token = Token("access id", "auth id", "subj")
    whenever(tokenRepository.findByAuthJwtId(anyString())).thenReturn(
      listOf(
        token,
      ),
    )

    webTestClient.delete().uri { it.path("/token").queryParam("authJwtId", "4+QpGwPH2X/3KOAda+3tlv/HjVHWo=").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
      .exchange()
      .expectStatus().isOk

    verify(tokenRepository).findByAuthJwtId("4+QpGwPH2X/3KOAda+3tlv/HjVHWo=")
  }

  @Test
  fun `revoke token incorrect role`() {
    webTestClient.delete().uri { it.path("/token").queryParam("authJwtId", "auth_jwt_id").build() }
      .headers(setAuthorisation(roles = listOf("ROLE_INCORRECT")))
      .exchange()
      .expectStatus().isForbidden

    verifyNoInteractions(tokenRepository)
  }

  private fun String.loadJson(): String = TokenResourceTest::class.java.getResource("$this.json").readText()
}
