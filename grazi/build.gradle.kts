plugins {
    java apply true
    id("io.gitlab.arturbosch.detekt").version("1.0.0-RC14") apply true
    id("tanvd.kosogor") version "1.0.4" apply true
    id("org.jetbrains.intellij") version "0.4.5" apply true
    kotlin("jvm") version "1.3.21" apply true
}

repositories {
    mavenCentral()
    jcenter()
}

intellij {
    pluginName = "Grazi"
    version = "2018.3.5"
    downloadSources = true
    setPlugins("org.intellij.plugins.markdown:183.5153.1", "org.jetbrains.kotlin:1.3.21-release-IJ2018.3-1")
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

configurations.all {
    exclude("org.slf4j", "slf4j-api")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))
    compile("org.languagetool", "languagetool-core", "4.4")
    compile("org.apache.commons", "commons-lang3", "3.5")
    compile("org.languagetool", "language-all", "4.4")
    testCompile("org.junit.jupiter", "junit-jupiter-api", "5.0.2")
    compile("com.github.ben-manes.caffeine", "caffeine", "2.7.0")
}
