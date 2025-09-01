import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.2"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

group = "com.intellij"
version = "0.1"

repositories {
    mavenCentral()
}

intellij {
    version.set("LATEST-EAP-SNAPSHOT")
    type.set("CL")
}

sourceSets {
    main {
        kotlin.srcDirs("src")
        java.srcDirs("src", "gen")
        resources.srcDirs("resources")
    }

    test {
        kotlin.srcDirs("test")
        java.srcDirs("testGen")
        resources.srcDirs("testData")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
      sourceCompatibility = "21"
      targetCompatibility = "21"
    }

    withType<KotlinCompile> {
        dependsOn(generateParser)

      kotlinOptions.jvmTarget = "21"
        kotlinOptions.freeCompilerArgs += "-Xjvm-default=all"
    }

    generateParser {
        dependsOn(generateLexer)

        sourceFile.set(File("test/com/intellij/dts/pp/test/impl/test.bnf"))
        pathToParser.set("com/intellij/dts/pp/test/impl/TestParser.java")

        pathToPsiRoot.set("com/intellij/dts/pp/test/impl/psi")
        targetRootOutputDir.set(File("testGen"))
        purgeOldFiles.set(true)
    }

    val generateTestLexer by register<GenerateLexerTask>("generateTestLexer") {
        sourceFile.set(File("test/com/intellij/dts/pp/test/impl/test.flex"))

        targetOutputDir.set(File("testGen/com/intellij/dts/pp/test/impl"))
        purgeOldFiles.set(true)
    }

    generateLexer {
        dependsOn(generateTestLexer)

        sourceFile.set(File("src/com/intellij/dts/pp/lang/lexer/pp.flex"))

        targetOutputDir.set(File("gen/com/intellij/dts/pp/lang/lexer"))
        purgeOldFiles.set(true)
    }

    runIde {
        enabled = false
    }
}