@file:Suppress("DEPRECATION")

package uk.gov.justice.digital.hmpps.tokenverification.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.time.Duration
import java.util.*
import java.util.UUID


@Component
class JwtAuthHelper {
  private val keyPair: KeyPair

  init {
    val gen = KeyPairGenerator.getInstance("RSA")
    gen.initialize(2048)
    keyPair = gen.generateKeyPair()
  }

  fun createJwt(parameters: JwtParameters): String {
    val claims = HashMap<String, Any>()
    claims["user_name"] = parameters.subject
    claims["client_id"] = "elite2apiclient"
    if (!parameters.roles.isNullOrEmpty()) claims["authorities"] = parameters.roles
    if (!parameters.scope.isNullOrEmpty()) claims["scope"] = parameters.scope
    return Jwts.builder()
        .setId(parameters.jwtId)
        .setSubject(parameters.subject)
        .addClaims(claims)
        .setExpiration(Date(System.currentTimeMillis() + parameters.expiryTime.toMillis()))
        .signWith(SignatureAlgorithm.RS256, keyPair.private)
        .compact()
  }

  data class JwtParameters(val subject: String,
                           val scope: List<String>? = listOf(),
                           val roles: List<String>? = listOf(),
                           val expiryTime: Duration = Duration.ofHours(1),
                           val jwtId: String = UUID.randomUUID().toString())
}
