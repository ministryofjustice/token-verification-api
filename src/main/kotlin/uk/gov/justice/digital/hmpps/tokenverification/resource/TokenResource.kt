package uk.gov.justice.digital.hmpps.tokenverification.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import uk.gov.justice.digital.hmpps.tokenverification.service.TokenService

@RestController
@Validated
@RequestMapping("/token", produces = [MediaType.APPLICATION_JSON_VALUE])
class TokenResource(private val tokenService: TokenService) {
  @PostMapping("verify")
  fun verifyToken(@RequestBody jwt: String): TokenDto = tokenService.verifyToken(jwt)

  @ApiIgnore
  @PostMapping("{authJwtId}")
  fun addToken(@PathVariable authJwtId: String, @RequestBody jwt: String) {
    tokenService.addToken(authJwtId, jwt)
  }

  @ApiIgnore
  @PostMapping("refresh/{accessJwtId}")
  fun addRefreshToken(@PathVariable accessJwtId: String, @RequestBody jwt: String) {
    tokenService.addRefreshToken(accessJwtId, jwt)
  }

  @ApiIgnore
  @DeleteMapping("{authJwtId}")
  fun revokeTokens(@PathVariable authJwtId: String) {
    tokenService.revokeTokens(authJwtId)
  }
}

@JsonInclude(NON_NULL)
data class TokenDto(val active: Boolean)
