package uk.gov.justice.digital.hmpps.tokenverification.service

import com.nimbusds.jwt.SignedJWT
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.tokenverification.data.Token
import uk.gov.justice.digital.hmpps.tokenverification.data.TokenRepository
import uk.gov.justice.digital.hmpps.tokenverification.resource.TokenDto
import javax.validation.ValidationException

@Service
class TokenService(private val tokenRepository: TokenRepository) {
  fun verifyToken(jwt: String): TokenDto {
    val parsedJwt = validateJwt(jwt)
    log.info("Verifying token with id {}", parsedJwt.jwtClaimsSet.jwtid)

    return tokenRepository.findById(parsedJwt.jwtClaimsSet.jwtid).map {
      TokenDto(active = true)
    }.orElseGet { TokenDto(active = false) }
  }

  fun addToken(authJwtId: String, jwt: String) {
    val parsedJwt = validateJwt(jwt)

    log.info("Adding token with authJwtId of {} and id {}", authJwtId, parsedJwt.jwtClaimsSet.jwtid)

    tokenRepository.save(Token(parsedJwt.jwtClaimsSet.jwtid, authJwtId, parsedJwt.jwtClaimsSet.subject))
  }

  fun addRefreshToken(accessJwtId: String, jwt: String) {
    val parsedJwt = validateJwt(jwt)

    val accessToken = tokenRepository.findById(accessJwtId)
    accessToken.ifPresent {
      log.info("Adding refresh token with authJwtId of {} and id {}", it.authJwtId, parsedJwt.jwtClaimsSet.jwtid)
      tokenRepository.save(Token(parsedJwt.jwtClaimsSet.jwtid, it.authJwtId, parsedJwt.jwtClaimsSet.subject))
    }
  }

  fun revokeTokens(authJwtId: String) {
    log.info("Removing all tokens for authJwtId of {}", authJwtId)
    val tokens = tokenRepository.findByAuthJwtId(authJwtId)
    log.debug("Deleting ${tokens.size} for auth token")
    tokens.forEach { tokenRepository.delete(it) }
  }

  private fun validateJwt(jwt: String): SignedJWT {
    val parsedJwt = SignedJWT.parse(jwt)

    if (parsedJwt.jwtClaimsSet.jwtid.isNullOrBlank()) {
      log.info("Unable to retrieve jwt id due to jwtId being null or blank")
      throw ValidationException("Unable to find jwtId from token")
    }

    if (parsedJwt.jwtClaimsSet.subject.isNullOrBlank()) {
      log.info("Unable to retrieve jwt id due to subject being null or blank")
      throw ValidationException("Unable to find subject from token")
    }

    // TODO: verify JWT

    // TODO: check not expired?
    return parsedJwt
  }

  companion object {
    private val log = LoggerFactory.getLogger(TokenService::class.java)
  }
}
