package uk.gov.justice.digital.hmpps.tokenverification.resource

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(RedisExtension::class)
class TokenResourceRedisTest : IntegrationTest() {
  @Test
  fun `workflow for token`() {
    val jwt = jwtHelper.createJwt(subject = "bob")

    // token shouldn't be valid before we start
    verifyToken(jwt, false)

    // add the token for a given auth jwt id
    addToken("auth_id", jwt)

    // now check is valid
    verifyToken(jwt)

    // revoke all token for the auth jwt id
    revokeTokens("auth_id")

    // token should now be invalid
    verifyToken(jwt, false)
  }

  @Test
  fun `workflow for refresh token`() {
    val accessTokenId = "access_jwt_id"

    val accessJwt = jwtHelper.createJwt(subject = "bob", jwtId = accessTokenId)
    // add the token for a given auth jwt id
    addToken("auth id_refresh", accessJwt)

    val refreshJwt = jwtHelper.createJwt(subject = "bob")
    // and a refresh token for it
    addRefreshToken(accessTokenId, refreshJwt)

    // now check is valid
    verifyToken(refreshJwt)

    // revoke all token for the auth jwt id
    revokeTokens("auth id_refresh")

    // tokens should now be invalid
    verifyToken(accessJwt, false)
    verifyToken(refreshJwt, false)
  }

  @Test
  fun `revoke only revokes for auth jwt id`() {
    val jwt = jwtHelper.createJwt(subject = "bob")
    // add the token for a given auth jwt id
    addToken("auth_id_revoke", jwt)

    val otherJwt = jwtHelper.createJwt(subject = "bob")
    // add the token for a given auth jwt id
    addToken("auth_id_other", otherJwt)

    // now check is valid
    verifyToken(jwt)

    // revoke all token for the auth jwt id
    revokeTokens("auth_id_revoke")

    // token should now be invalid
    verifyToken(jwt, false)

    // but other token should still be valid
    verifyToken(otherJwt)
  }

  private fun verifyToken(jwt: String, found: Boolean = true) {
    webTestClient.post().uri("/token/verify")
        .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("active", found)
  }

  private fun addToken(authId: String, jwt: String) {
    webTestClient.post().uri { it.path("/token").queryParam("authJwtId", authId).build() }
        .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isOk
  }

  @Suppress("SameParameterValue")
  private fun addRefreshToken(accessJwtId: String, jwt: String) {
    webTestClient.post().uri { it.path("/token/refresh").queryParam("accessJwtId", accessJwtId).build() }
        .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
        .bodyValue(jwt)
        .exchange()
        .expectStatus().isOk
  }

  private fun revokeTokens(authId: String) {
    webTestClient.delete().uri { it.path("/token").queryParam("authJwtId", authId).build() }
        .headers(setAuthorisation(roles = listOf("ROLE_AUTH_TOKEN_VERIFICATION")))
        .exchange()
        .expectStatus().isOk
  }
}
