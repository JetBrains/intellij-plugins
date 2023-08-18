import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.13.3"
    id("org.jetbrains.grammarkit") version "2022.3.1"
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
        resources.srcDirs("testData")
    }
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
        sourceFile.set(File("src/com/intellij/dts/pp/lang/lexer/pp.flex"))

        targetDir.set("gen/com/intellij/dts/pp/lang/lexer")
        targetClass.set("PpLexer")
        purgeOldFiles.set(true)
    }
}