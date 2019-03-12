plugins {
  kotlin("jvm") version "1.3.21"
  id("org.jetbrains.intellij") version "0.4.4"
}

group = "name.kropp.intellij"
version = "1.6.1"

repositories {
  mavenCentral()
}

intellij {
  version = "2018.3"
  pluginName = rootProject.name
  updateSinceUntilBuild = false
}

kotlin.sourceSets["main"].kotlin.srcDir("gen")

dependencies {
  api(kotlin("stdlib-jdk8"))

  testImplementation("org.hamcrest", "hamcrest", "2.1")
}