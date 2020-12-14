package uk.gov.justice.digital.hmpps.tokenverification.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.validation.ValidationException

@RestControllerAdvice
class TokenVerificationExceptionHandler {
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  @ExceptionHandler(BadJwtException::class)
  fun handleParseException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Parse exception: {}", e.message)
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(
        ErrorResponse(
          status = HttpStatus.BAD_REQUEST,
          userMessage = "Bad JWT: ${e.message}",
          developerMessage = e.message
        )
      )
  }

  companion object {
    private val log = LoggerFactory.getLogger(TokenVerificationExceptionHandler::class.java)
  }
}

data class ErrorResponse(
  val status: Int,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
