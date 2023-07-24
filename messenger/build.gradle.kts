plugins {
  java
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  pmd
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
  implementation(project(":messenger-rest-api"))
  implementation(project(":messenger-queue-api"))
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.postgresql:postgresql")
  implementation("org.liquibase:liquibase-core")
  implementation("org.springframework.kafka:spring-kafka")
  implementation(libs.mapstruct)
  annotationProcessor(libs.mapstruct.processor)
  implementation(libs.logbook.spring.boot.starter)
}

pmd {
  isIgnoreFailures = false
  ruleSetFiles(rootDir.resolve("config/pmd/RuleSet.xml"))
  ruleSets = emptyList()
}
