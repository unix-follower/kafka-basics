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
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.kafka:spring-kafka")
  implementation("io.projectreactor.kafka:reactor-kafka")
  implementation(libs.logbook.netty)
  implementation(libs.logbook.spring.webflux)
}

pmd {
  isIgnoreFailures = false
  ruleSetFiles(rootDir.resolve("config/pmd/RuleSet.xml"))
  ruleSets = emptyList()
}
