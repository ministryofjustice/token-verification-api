plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.1.4"
  kotlin("plugin.spring") version "1.4.32"
  kotlin("plugin.jpa") version "1.4.32"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  implementation("io.springfox:springfox-boot-starter:3.0.0")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.google.guava:guava:30.1.1-jre")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("it.ozimov:embedded-redis:0.7.3")
  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
}
