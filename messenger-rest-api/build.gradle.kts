plugins {
  id("java")
  id("org.springframework.boot")
  id("io.spring.dependency-management")
  pmd
}

dependencies {
  implementation("jakarta.validation:jakarta.validation-api")
}

tasks {
  bootJar {
    enabled = false
  }
}

pmd {
  isIgnoreFailures = false
  ruleSetFiles(rootDir.resolve("config/pmd/RuleSet.xml"))
  ruleSets = emptyList()
}
