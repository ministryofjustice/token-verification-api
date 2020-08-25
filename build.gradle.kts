plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "1.0.0"
  kotlin("plugin.spring") version "1.4.0"
  kotlin("plugin.jpa") version "1.4.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  implementation("io.springfox:springfox-swagger2:2.9.2")
  implementation("io.springfox:springfox-swagger-ui:2.9.2")
  implementation("io.springfox:springfox-bean-validators:2.9.2")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.google.guava:guava:29.0-jre")
  implementation("com.nimbusds:nimbus-jose-jwt:8.20")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("it.ozimov:embedded-redis:0.7.3")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}
