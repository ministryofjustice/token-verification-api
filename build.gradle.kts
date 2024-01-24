plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.15.1"
  kotlin("plugin.spring") version "1.9.22"
  kotlin("plugin.jpa") version "1.9.22"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
  testImplementation { exclude(module = "slf4j-simple") }
}

dependencyCheck {
  suppressionFiles.add("token-verification-suppressions.xml")
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("org.springframework.boot:spring-boot-starter-data-redis")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.google.guava:guava:33.0.0-jre")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("it.ozimov:embedded-redis:0.7.3")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.3")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "21"
    }
  }
}
