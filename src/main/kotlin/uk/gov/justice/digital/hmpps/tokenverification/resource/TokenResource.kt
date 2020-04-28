package uk.gov.justice.digital.hmpps.tokenverification.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore
import uk.gov.justice.digital.hmpps.tokenverification.config.ErrorResponse
import uk.gov.justice.digital.hmpps.tokenverification.service.TokenService

@RestController
@Validated
@RequestMapping("/token", produces = [MediaType.APPLICATION_JSON_VALUE])
class TokenResource(private val tokenService: TokenService) {
  @PostMapping("verify")

  @ApiOperation(value = "Verify that a JWT is still valid.",
      notes = """A successful request to this API will return a <code>HTTP 200 - Success</code>, but this doesn't
                 indicate that the JWT is valid.  You need to check the boolean <code>active</code> flag which is
                 returned in the payload body.""")
  @ApiResponses(value = [ApiResponse(code = 400, message = "Bad request.  The JWT is invalid or has expired.", response = ErrorResponse::class, responseContainer = "List")])
  fun verifyToken(@RequestBody @ApiParam(value = "JWT to check")
                  jwt: String): TokenDto = tokenService.verifyToken(jwt)

  @ApiIgnore
  @PreAuthorize("hasRole('AUTH_TOKEN_VERIFICATION')")
  @PostMapping
  fun addToken(@RequestParam(value = "authJwtId", required = true) authJwtId: String, @RequestBody jwt: String) {
    tokenService.addToken(authJwtId, jwt)
  }

  @ApiIgnore
  @PreAuthorize("hasRole('AUTH_TOKEN_VERIFICATION')")
  @PostMapping("refresh")
  fun addRefreshToken(@RequestParam(value = "accessJwtId", required = true) accessJwtId: String, @RequestBody jwt: String) {
    tokenService.addRefreshToken(accessJwtId, jwt)
  }

  @ApiIgnore
  @PreAuthorize("hasRole('AUTH_TOKEN_VERIFICATION')")
  @DeleteMapping
  fun revokeTokens(@RequestParam(value = "authJwtId", required = true) authJwtId: String) {
    tokenService.revokeTokens(authJwtId)
  }
}

@JsonInclude(NON_NULL)
data class TokenDto(val active: Boolean)
