package uk.gov.justice.digital.hmpps.tokenverification.data

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenRepository : CrudRepository<Token, String> {
  fun findByAuthJwtId(authJwtId: String): List<Token>
  fun findBySubject(subject: String): List<Token>
}

@RedisHash(value = "token", timeToLive = 86400) // expire all tokens after a day
data class Token(
  @Id
  val jwtId: String,
  @Indexed
  val authJwtId: String,
  @Indexed
  val subject: String,
)
