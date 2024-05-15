plugins {
  id("java")
  id("application")
  id("org.jetbrains.kotlin.jvm") version "1.9.23"
}

group = "com.intellij"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
  implementation(kotlin("stdlib-jdk8"))
  implementation ("com.bertramlabs.plugins:hcl4j:0.9.1")
}

sourceSets {
  main {
    kotlin { srcDir("src") }
  }
}

application {
  applicationName = "ls-schemas-extractor"
  mainClass.set("TerraformProvidersMetadataBuilder")
}

tasks {
  val distributionName = application.applicationName

  named<Zip>("distZip") {
    archiveFileName.set("$distributionName.zip")
  }

  named<Tar>("distTar") {
    archiveFileName.set("$distributionName.tar")
  }
}