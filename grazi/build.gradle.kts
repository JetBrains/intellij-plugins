import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import tanvd.grazi.channel

group = "tanvd.grazi"
version = "2019.1-4.2.$channel"

plugins {
    id("tanvd.kosogor") version "1.0.7" apply true
    id("io.gitlab.arturbosch.detekt") version ("1.0.0-RC14") apply true
    id("org.jetbrains.intellij") apply true
    kotlin("jvm") version "1.3.41" apply true
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }

    apply {
        plugin("tanvd.kosogor")
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jetbrains.intellij")
        plugin("kotlin")
    }

    tasks.withType<KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            languageVersion = "1.3"
            apiVersion = "1.3"
        }
    }


    detekt {
        parallel = true
        failFast = false
        config = files(File(project.rootProject.projectDir, "buildScripts/detekt/detekt.yml"))
        reports {
            xml {
                enabled = false
            }
            html {
                enabled = false
            }
        }
    }
}
