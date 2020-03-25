package uk.gov.justice.digital.hmpps.tokenverification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TokenVerificationApplication

fun main(args: Array<String>) {
  runApplication<TokenVerificationApplication>(*args)
}
