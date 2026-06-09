plugins {
    kotlin("jvm")
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}
dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}