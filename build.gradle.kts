import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.1.2"
  kotlin("plugin.spring") version "2.0.21"
  kotlin("plugin.jpa") version "2.0.21"
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

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.google.guava:guava:33.4.0-jre")

  testImplementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation("it.ozimov:embedded-redis:0.7.3")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.6")
  testImplementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

kotlin {
  jvmToolchain(21)
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
  }
}
