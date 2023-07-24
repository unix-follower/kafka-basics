rootProject.name = "kafka-basics"

include(
  "messenger-queue-api",
  "messenger-rest-api",
  "messenger",
  "messenger-consumer",
  "reactive-messenger",
  "reactive-messenger-consumer",
)

pluginManagement {
  plugins {
    id("org.springframework.boot") version "3.1.1"
    id("io.spring.dependency-management") version "1.1.0"
  }
}

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      library("r2dbc-postgresql", "org.postgresql:r2dbc-postgresql:1.0.1.RELEASE")
      library("r2dbc-pool", "io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
      library("r2dbc-spi", "io.r2dbc:r2dbc-spi:1.0.0.RELEASE")
      library("logbook-spring-boot-starter", "org.zalando:logbook-spring-boot-starter:3.1.0")
      library("logbook-netty", "org.zalando:logbook-netty:3.1.0")
      library("logbook-spring-webflux", "org.zalando:logbook-spring-webflux:3.1.0")
      library("mapstruct", "org.mapstruct:mapstruct:1.5.5.Final")
      library("mapstruct-processor", "org.mapstruct:mapstruct-processor:1.5.5.Final")
    }
  }
}
