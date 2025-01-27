repositories {
  mavenCentral()
}

rootProject.extensions.add("platform.version", "LATEST-EAP-SNAPSHOT")

rootProject.extensions.add("gradle.version", "8.5")

rootProject.extensions.add("junit.version", "4.13.2")

rootProject.extensions.add("kotlin.jvmTarget", "21")
rootProject.extensions.add("kotlin.freeCompilerArgs", listOf("-Xjvm-default=all"))

rootProject.extensions.add("java.sourceCompatibility", "21")
rootProject.extensions.add("java.targetCompatibility", "21")