apply(from = "../contrib-configuration/common.gradle.kts")

fun properties(key: String) = project.findProperty(key).toString()

plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.7.10"
  id("org.jetbrains.intellij") version "1.10.0"
  id("org.jetbrains.changelog") version "1.3.1"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

idea {
  module {
    generatedSourceDirs.add(file("gen"))
  }
}

java {
  sourceSets {
    main {
      java.srcDir("gen")
      resources.srcDir("resources")
    }

    test {
      java.srcDirs("test")
      resources.srcDir("test/testData")
    }
  }
}

kotlin {
  sourceSets {
    main {
      kotlin.srcDir("src")
      resources.srcDir("resources")
    }

    test {
      kotlin.srcDir("test")
      resources.srcDir("test/testData")
    }
  }

  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(properties("jvmVersion")))
  }
}

intellij {
  pluginName.set(properties("pluginName"))
  version.set(properties("platformVersion"))
  type.set(properties("platformType"))
  plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

tasks {
  compileKotlin {
    kotlinOptions {
      jvmTarget = properties("jvmVersion")
    }
  }

  prepareSandbox {
    doLast {
      copy {
        from("${project.projectDir}/gen/language-server")
        into("${destinationDir.path}/${properties("pluginName")}/language-server")
      }
    }
  }

  wrapper {
    gradleVersion = properties("gradleVersion")
  }

  runPluginVerifier {
    ideVersions.set(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
  }

  patchPluginXml {
    version.set(properties("pluginVersion"))
    sinceBuild.set(properties("pluginSinceBuild"))
    untilBuild.set(properties("pluginUntilBuild"))
  }
}
