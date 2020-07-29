import org.jetbrains.kotlin.gradle.tasks.*

plugins {
  kotlin("jvm") version "1.3.72"
  id("org.jetbrains.intellij") version "0.4.17"
}

group = "name.kropp.intellij"
version = "3.5"

repositories {
  mavenCentral()
}

intellij {
  version = "2020.1"
  pluginName = rootProject.name
  updateSinceUntilBuild = false

  setPlugins("com.jetbrains.sh:201.6668.74")
}

sourceSets["main"].java.srcDir("gen")

tasks.withType<JavaCompile> {
  sourceCompatibility = "1.8"
  targetCompatibility = "1.8"
}

tasks.withType<KotlinCompile> {
  sourceCompatibility = "1.8"
  targetCompatibility = "1.8"
  kotlinOptions {
    jvmTarget = "1.8"
    apiVersion = "1.3"
    languageVersion = "1.3"
  }
}

dependencies {
}