import org.jetbrains.kotlin.gradle.tasks.*

plugins {
  kotlin("jvm") version "1.3.72"
  id("org.jetbrains.intellij") version "0.4.21"
}

group = "name.kropp.intellij"
version = "3.5.1"

repositories {
  mavenCentral()
}

intellij {
  version = "2020.2"
  pluginName = rootProject.name
  updateSinceUntilBuild = false

  setPlugins("org.jetbrains.plugins.sh:202.6397.21")
  setPlugins("org.jetbrains.plugins.terminal:202.6397.21")
}

sourceSets["main"].java.srcDir("gen")

tasks.withType<JavaCompile> {
  sourceCompatibility = "11"
  targetCompatibility = "11"
}

tasks.withType<KotlinCompile> {
  sourceCompatibility = "11"
  targetCompatibility = "11"
  kotlinOptions.jvmTarget = "11"
}

dependencies {
}