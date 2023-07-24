plugins {
  id("java")
  pmd
}

pmd {
  isIgnoreFailures = false
  ruleSetFiles(rootDir.resolve("config/pmd/RuleSet.xml"))
  ruleSets = emptyList()
}
