package uk.gov.justice.digital.hmpps.tokenverification.data

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenRepository : CrudRepository<Token, String> {
  fun findByAuthId(authId: String): List<Token>
  fun findByUsername(username: String): List<Token>
}

@RedisHash(timeToLive = 86400) // expire all tokens after a day
data class Token(
    @Id
    val tokenId: String,
    @Indexed
    val authId: String,
    @Indexed
    val username: String)
