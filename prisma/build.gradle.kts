apply(from = "../contrib-configuration/common.gradle.kts")

fun properties(key: String) = project.findProperty(key).toString()
fun ext(name: String): String = rootProject.extensions[name] as? String ?: error("Property `$name` is not defined")


plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm")
  id("org.jetbrains.intellij")
  id("org.jetbrains.changelog") version "1.3.1"
}

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
      resources.srcDir("testData")
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
      resources.srcDir("testData")
    }
  }
}

intellij {
  pluginName.set("Prisma ORM")
  plugins.set(listOf("JavaScript"))

  version.set("LATEST-EAP-SNAPSHOT")
  type.set("IU")
}

tasks {
  java {
    sourceCompatibility = JavaVersion.toVersion(ext("java.sourceCompatibility"))
    targetCompatibility = JavaVersion.toVersion(ext("java.targetCompatibility"))
  }

  compileKotlin {
    kotlinOptions.jvmTarget = ext("kotlin.jvmTarget")
    @Suppress("UNCHECKED_CAST")
    kotlinOptions.freeCompilerArgs = rootProject.extensions["kotlin.freeCompilerArgs"] as List<String>
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
    gradleVersion = ext("gradle.version")
  }

  runIde {
    autoReloadPlugins.set(false)
  }
}
