package uk.gov.justice.digital.hmpps.tokenverification.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration {
  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    http {
      headers { frameOptions { sameOrigin = true } }
      sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
      // Can't have CSRF protection as requires session
      csrf { disable() }
      authorizeHttpRequests {
        listOf(
          "/webjars/**", "/favicon.ico", "/csrf",
          "/health/**", "/info",
          "/v3/api-docs/**", "/api/swagger.json", "/swagger-ui/**",
          "/v3/api-docs", "/swagger-ui.html",
          "/swagger-resources", "/swagger-resources/configuration/ui", "/swagger-resources/configuration/security",
        ).forEach { authorize(it, permitAll) }
        authorize(anyRequest, authenticated)
      }
      oauth2ResourceServer { jwt { jwtAuthenticationConverter = AuthAwareTokenConverter() } }
    }
    return http.build()
  }
}
class AuthAwareTokenConverter : Converter<Jwt, AbstractAuthenticationToken> {
  private val jwtGrantedAuthoritiesConverter: Converter<Jwt, Collection<GrantedAuthority>> =
    JwtGrantedAuthoritiesConverter()

  override fun convert(jwt: Jwt): AbstractAuthenticationToken = AuthAwareAuthenticationToken(jwt, findPrincipal(jwt.claims), extractAuthorities(jwt))

  private fun findPrincipal(claims: Map<String, Any?>): String = when {
    claims.containsKey("user_name") -> claims["user_name"] as String
    claims.containsKey("user_id") -> claims["user_id"] as String
    else -> claims["client_id"] as String
  }

  @Suppress("UNCHECKED_CAST", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
  private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
    val authorities = jwtGrantedAuthoritiesConverter.convert(jwt).toMutableSet()
    if (jwt.claims.containsKey("authorities")) {
      authorities.addAll(
        (jwt.claims["authorities"] as Collection<String?>)
          .filterNotNull()
          .map { SimpleGrantedAuthority(it) }
          .toSet(),
      )
    }
    return authorities.toSet()
  }
}

internal class AuthAwareAuthenticationToken(jwt: Jwt, private val aPrincipal: String, authorities: Collection<GrantedAuthority>) : JwtAuthenticationToken(jwt, authorities) {
  override fun getPrincipal(): String = aPrincipal
}
