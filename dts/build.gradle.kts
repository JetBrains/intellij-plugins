import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.grammarkit.tasks.GenerateLexerTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.13.3"
    id("org.jetbrains.grammarkit") version "2022.3.1"
}

group = "com.jetbrains"
version = "0.5"

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
        resources.srcDirs("testData")
    }
}

val generatePpLexer = task<GenerateLexerTask>("generatePpLexer") {
    sourceFile.set(File("src/com/intellij/dts/lang/lexer/pp.flex"))

    targetDir.set("gen/com/intellij/dts/lang/lexer")
    targetClass.set("PpLexer")
    purgeOldFiles.set(true)
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<KotlinCompile> {
        dependsOn(generateParser)

        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs += "-Xjvm-default=all"
    }

    generateLexer {
        dependsOn(generatePpLexer)

        sourceFile.set(File("src/com/intellij/dts/lang/lexer/dts.flex"))

        targetDir.set("gen/com/intellij/dts/lang/lexer")
        targetClass.set("DtsLexer")
        purgeOldFiles.set(true)
    }

    generateParser {
        dependsOn(generateLexer)

        sourceFile.set(File("src/com/intellij/dts/lang/parser/dts.bnf"))

        targetRoot.set("gen")
        pathToParser.set("com/intellij/dts/lang/parser/DtsParser.java")
        pathToPsiRoot.set("com/intellij/dts/lang/psi")
        purgeOldFiles.set(true)
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("232.*")
    }
}