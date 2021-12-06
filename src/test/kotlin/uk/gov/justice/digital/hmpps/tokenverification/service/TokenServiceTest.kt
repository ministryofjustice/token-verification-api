package uk.gov.justice.digital.hmpps.tokenverification.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.BadJwtException
import uk.gov.justice.digital.hmpps.tokenverification.data.Token
import uk.gov.justice.digital.hmpps.tokenverification.data.TokenRepository
import uk.gov.justice.digital.hmpps.tokenverification.resource.TokenDto
import java.time.Duration
import java.util.Optional
import javax.validation.ValidationException

@Suppress("ClassName")
class TokenServiceTest {
  private val tokenRepository: TokenRepository = mock()
  private val jwtHelper = JwtAuthHelper()
  private val tokenService = TokenService(tokenRepository, jwtHelper.jwtDecoder())

  @Nested
  inner class verifyToken {
    @Test
    fun `verify token invalid token`() {
      assertThatThrownBy { tokenService.verifyToken("not a jwt") }
        .isInstanceOf(BadJwtException::class.java)
        .hasMessageContaining("Invalid JWT serialization")
    }

    @Test
    fun `verify token jwt blank`() {
      val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "")
      assertThatThrownBy { tokenService.verifyToken(jwt) }
        .isInstanceOf(ValidationException::class.java)
        .hasMessage("Unable to find jwtId from token")
    }

    @Test
    fun `verify token subject blank`() {
      val jwt = jwtHelper.createJwt(subject = "")
      assertThatThrownBy { tokenService.verifyToken(jwt) }
        .isInstanceOf(ValidationException::class.java)
        .hasMessage("Unable to find subject from token")
    }

    @Test
    fun `verify token not found`() {
      val jwt = jwtHelper.createJwt(subject = "bob")
      val tokenDto = tokenService.verifyToken(jwt)
      assertThat(tokenDto).isEqualTo(TokenDto(active = false))
    }

    @Test
    fun `verify token expired`() {
      val jwt = jwtHelper.createJwt(subject = "bob", expiryTime = Duration.ofHours(-1))
      assertThatThrownBy { tokenService.verifyToken(jwt) }
        .isInstanceOf(BadJwtException::class.java)
    }

    @Test
    fun `verify token signature invalid`() {
      // creating new instance of auth helper with generate new keys
      val jwt = JwtAuthHelper().createJwt(subject = "bob")
      assertThatThrownBy { tokenService.verifyToken(jwt) }
        .isInstanceOf(BadJwtException::class.java)
        .hasMessageContaining("Signed JWT rejected")
    }

    @Test
    fun `verify token`() {
      val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "jwt id")
      whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(Token("access id", "auth id", "subj")))
      val tokenDto = tokenService.verifyToken(jwt)
      assertThat(tokenDto).isEqualTo(TokenDto(active = true))
      verify(tokenRepository).findById("jwt id")
    }
  }

  @Nested
  inner class addToken {
    @Test
    fun `add token`() {
      val jwt = jwtHelper.createJwt(subject = "bob", jwtId = "jwt id")
      tokenService.addToken("auth id", jwt)
      verify(tokenRepository).save(Token("jwt id", "auth id", "bob"))
    }
  }

  @Nested
  inner class addRefreshToken {
    @Test
    fun `refresh token, access token not found`() {
      val jwt = jwtHelper.createJwt("bob")

      whenever(tokenRepository.findById(anyString())).thenReturn(Optional.empty())

      tokenService.addRefreshToken("access id", jwt)

      verify(tokenRepository).findById("access id")
      verifyNoMoreInteractions(tokenRepository)
    }

    @Test
    fun `refresh token`() {
      val jwt = jwtHelper.createJwt(subject = "joe", jwtId = "jwt id")

      whenever(tokenRepository.findById(anyString())).thenReturn(Optional.of(Token("access id", "auth id", "subj")))

      tokenService.addRefreshToken("access id", jwt)

      verify(tokenRepository).save(Token("jwt id", "auth id", "joe"))
    }
  }

  @Nested
  inner class revokeTokens {
    @Test
    fun `revoke token`() {
      val token = Token("jwt id", "auth id", "bob")
      val token2 = Token("jwt id2", "auth id", "joe")
      whenever(tokenRepository.findByAuthJwtId(anyString())).thenReturn(listOf(token, token2))

      tokenService.revokeTokens("auth id")
      verify(tokenRepository).delete(token)
      verify(tokenRepository).delete(token2)
    }

    @Test
    fun `revoke token no tokens found`() {
      whenever(tokenRepository.findByAuthJwtId(anyString())).thenReturn(listOf())

      tokenService.revokeTokens("auth id")
      verify(tokenRepository).findByAuthJwtId("auth id")
      verifyNoMoreInteractions(tokenRepository)
    }
  }
}
