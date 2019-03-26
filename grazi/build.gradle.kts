plugins {
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
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))
    compile("org.languagetool","languagetool-core", "4.4")
}
