package uk.gov.justice.digital.hmpps.tokenverification.service

import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.tokenverification.data.Token
import uk.gov.justice.digital.hmpps.tokenverification.data.TokenRepository
import uk.gov.justice.digital.hmpps.tokenverification.resource.TokenDto
import javax.validation.ValidationException


@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Service
class TokenService(private val tokenRepository: TokenRepository, private val jwtDecoder: JwtDecoder) {
  fun verifyToken(jwt: String): TokenDto {
    val (jwtId, _) = validateJwt(jwt)
    log.info("Verifying token with id {}", jwtId)

    return tokenRepository.findById(jwtId).map {
      TokenDto(active = true)
    }.orElseGet { TokenDto(active = false) }
  }

  fun addToken(authJwtId: String, jwt: String) {
    val (jwtId, subject) = validateJwt(jwt)

    log.info("Adding token with authJwtId of {} and id {}", authJwtId, jwtId)

    tokenRepository.save(Token(jwtId, authJwtId, subject))
  }

  fun addRefreshToken(accessJwtId: String, jwt: String) {
    val (jwtId, subject) = validateJwt(jwt)

    val accessToken = tokenRepository.findById(accessJwtId)
    accessToken.ifPresent {
      log.info("Adding refresh token with authJwtId of {} and id {}", it.authJwtId, jwtId)
      tokenRepository.save(Token(jwtId, it.authJwtId, subject))
    }
  }

  fun revokeTokens(authJwtId: String) {
    log.info("Removing all tokens for authJwtId of {}", authJwtId)
    val tokens = tokenRepository.findByAuthJwtId(authJwtId)
    log.debug("Deleting {} for auth token", tokens.size)
    tokens.forEach { tokenRepository.delete(it) }
  }

  private fun validateJwt(jwt: String): Pair<String, String> {
    val parsedJwt = jwtDecoder.decode(jwt)

    val jwtId = parsedJwt.getClaimAsString("jti")
    if (jwtId.isNullOrBlank()) {
      log.info("Unable to retrieve jwt id due to jti being null or blank")
      throw ValidationException("Unable to find jwtId from token")
    }

    val subject = parsedJwt.getClaimAsString("sub")
    if (subject.isNullOrBlank()) {
      log.info("Unable to retrieve jwt id due to subject being null or blank")
      throw ValidationException("Unable to find subject from token")
    }

    return Pair(jwtId, subject)
  }

  companion object {
    private val log = LoggerFactory.getLogger(TokenService::class.java)
  }
}
