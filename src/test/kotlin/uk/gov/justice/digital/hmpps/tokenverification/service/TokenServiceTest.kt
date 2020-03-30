package uk.gov.justice.digital.hmpps.tokenverification.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import uk.gov.justice.digital.hmpps.tokenverification.data.Token
import uk.gov.justice.digital.hmpps.tokenverification.data.TokenRepository
import uk.gov.justice.digital.hmpps.tokenverification.resource.TokenDto
import uk.gov.justice.digital.hmpps.tokenverification.service.JwtAuthHelper.JwtParameters
import java.text.ParseException
import java.util.*
import javax.validation.ValidationException

@Suppress("ClassName")
class TokenServiceTest {
  private val tokenRepository: TokenRepository = mock()
  private val tokenService = TokenService(tokenRepository)

  @Nested
  inner class verifyToken {
    @Test
    fun `verify token invalid token`() {
      assertThatThrownBy { tokenService.verifyToken("not a jwt") }
          .isInstanceOf(ParseException::class.java)
    }

    @Test
    fun `verify token jwt blank`() {
      val jwt = JwtAuthHelper().createJwt(JwtParameters(subject = "bob", jwtId = ""))
      assertThatThrownBy { tokenService.verifyToken(jwt) }
          .isInstanceOf(ValidationException::class.java)
          .hasMessage("Unable to find jwtId from token")
    }

    @Test
    fun `verify token subject blank`() {
      val jwt = JwtAuthHelper().createJwt(JwtParameters(subject = "", jwtId = "jwt id"))
      assertThatThrownBy { tokenService.verifyToken(jwt) }
          .isInstanceOf(ValidationException::class.java)
          .hasMessage("Unable to find subject from token")
    }

    @Test
    fun `verify token not found`() {
      val jwt = JwtAuthHelper().createJwt(JwtParameters(subject = "bob", jwtId = "jwt id"))
      val tokenDto = tokenService.verifyToken(jwt)
      assertThat(tokenDto).isEqualTo(TokenDto(active = false))
    }

    @Test
    fun `verify token`() {
      val jwt = JwtAuthHelper().createJwt(JwtParameters(subject = "bob", jwtId = "jwt id"))
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
      val jwt = JwtAuthHelper().createJwt(JwtParameters(subject = "bob", jwtId = "jwt id"))
      tokenService.addToken("auth id", jwt)
      verify(tokenRepository).save(Token("jwt id", "auth id", "bob"))
    }
  }

  @Nested
  inner class addRefreshToken {
    @Test
    fun `refresh token, access token not found`() {
      val jwt = JwtAuthHelper().createJwt(JwtParameters("bob"))

      whenever(tokenRepository.findById(anyString())).thenReturn(Optional.empty())

      tokenService.addRefreshToken("access id", jwt)

      verify(tokenRepository).findById("access id")
      verifyNoMoreInteractions(tokenRepository)
    }

    @Test
    fun `refresh token`() {
      val jwt = JwtAuthHelper().createJwt(JwtParameters(subject = "joe", jwtId = "jwt id"))

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
