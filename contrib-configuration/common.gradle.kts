repositories {
  mavenCentral()
  maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
  maven("https://www.jetbrains.com/intellij-repository/snapshots")
  maven("https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public")
  maven("https://download.jetbrains.com/teamcity-repository")
}

rootProject.extensions.add("gradle.version", "8.5")

rootProject.extensions.add("kotlin.jvmTarget", "17")
rootProject.extensions.add("kotlin.freeCompilerArgs", listOf("-Xjvm-default=all"))

rootProject.extensions.add("java.sourceCompatibility", "17")
rootProject.extensions.add("java.targetCompatibility", "17")