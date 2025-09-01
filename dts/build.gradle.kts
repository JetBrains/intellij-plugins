import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.intellij") version "1.17.2"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

group = "com.jetbrains"
version = "0.5"

repositories {
    mavenCentral()
}

intellij {
    plugins.set(listOf("com.intellij.clion"))
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

dependencies {
    implementation(project(":pp"))

    testImplementation("com.networknt:json-schema-validator:1.0.72");
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

    generateLexer {
        dependsOn("pp:generateLexer")

        sourceFile.set(File("src/com/intellij/dts/lang/lexer/dts.flex"))

        targetOutputDir.set(File("gen/com/intellij/dts/lang/lexer"))
        purgeOldFiles.set(true)
    }

    generateParser {
        dependsOn(generateLexer)
        dependsOn("pp:generateParser")

        sourceFile.set(File("src/com/intellij/dts/lang/parser/dts.bnf"))
        pathToParser.set("com/intellij/dts/lang/parser/DtsParser.java")

        targetRootOutputDir.set(File("gen"))
        pathToPsiRoot.set("com/intellij/dts/lang/psi")
        purgeOldFiles.set(true)
    }

    patchPluginXml {
        sinceBuild.set("221")
        untilBuild.set("241.*")
    }
}