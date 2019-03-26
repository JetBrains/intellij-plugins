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
    setPlugins("org.intellij.plugins.markdown:183.5153.1")
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(kotlin("reflect"))
    compile("org.languagetool","languagetool-core", "4.4")
    compile("org.languagetool","language-en", "4.4")
    compile("org.junit.jupiter","junit-jupiter-api", "5.0.2")
}
