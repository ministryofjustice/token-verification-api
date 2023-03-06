package uk.gov.justice.digital.hmpps.tokenverification.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.tokenverification.config.ErrorResponse
import uk.gov.justice.digital.hmpps.tokenverification.service.TokenService

@RestController
@Validated
@RequestMapping("/token", produces = [MediaType.APPLICATION_JSON_VALUE])
class TokenResource(private val tokenService: TokenService) {
  @Operation(
    summary = "Verify that a JWT is still valid.",
    description =
    """A successful request to this API will return a <code>HTTP 200 - Success</code>, but this doesn't
                 indicate that the JWT is valid.  You need to check the boolean <code>active</code> flag which is
                 returned in the payload body.""",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "OK",
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad request.  The JWT is invalid or has expired.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  @PostMapping("verify")
  fun verifyToken(
    @RequestHeader(HttpHeaders.AUTHORIZATION) bearerToken: String,
    @RequestBody
    @Parameter(name = "JWT to check")
    jwt: String?,
  ): TokenDto =
    tokenService.verifyToken(jwt ?: bearerToken.substringAfter("Bearer "))

  @Hidden
  @PreAuthorize("hasRole('AUTH_TOKEN_VERIFICATION')")
  @PostMapping
  fun addToken(@RequestParam(value = "authJwtId", required = true) authJwtId: String, @RequestBody jwt: String) {
    tokenService.addToken(authJwtId.replaceSpaceWithPlus(), jwt)
  }

  @Hidden
  @PreAuthorize("hasRole('AUTH_TOKEN_VERIFICATION')")
  @PostMapping("refresh")
  fun addRefreshToken(
    @RequestParam(value = "accessJwtId", required = true) accessJwtId: String,
    @RequestBody jwt: String,
  ) {
    tokenService.addRefreshToken(accessJwtId.replaceSpaceWithPlus(), jwt)
  }

  @Hidden
  @PreAuthorize("hasRole('AUTH_TOKEN_VERIFICATION')")
  @DeleteMapping
  fun revokeTokens(@RequestParam(value = "authJwtId", required = true) authJwtId: String) {
    tokenService.revokeTokens(authJwtId.replaceSpaceWithPlus())
  }

  private fun String.replaceSpaceWithPlus() = replace(" ", "+")
}

@JsonInclude(NON_NULL)
data class TokenDto(val active: Boolean)
