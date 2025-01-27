repositories {
  mavenCentral()
}

rootProject.extensions.add("platform.version", "2024.3")
rootProject.extensions.add("gradle.version", "8.5")

rootProject.extensions.add("kotlin.jvmTarget", "21")
rootProject.extensions.add("kotlin.freeCompilerArgs", listOf("-Xjvm-default=all"))

rootProject.extensions.add("java.sourceCompatibility", "21")
rootProject.extensions.add("java.targetCompatibility", "21")