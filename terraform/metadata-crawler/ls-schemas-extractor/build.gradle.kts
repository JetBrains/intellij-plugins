plugins {
  id("java")
  id("application")
  id("org.jetbrains.kotlin.jvm") version "1.8.0"
}

group = "com.intellij"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
}

sourceSets {
  main {
    kotlin { srcDir("src") }
  }
}

application {
  mainClass.set("TerraformProvidersMetadataBuilder")
}