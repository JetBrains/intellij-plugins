import org.jetbrains.intellij.tasks.*
import tanvd.grazi.*

group = rootProject.group
version = rootProject.version

intellij {
    pluginName = "Grazi"
    version = Versions.intellij
    downloadSources = true
    type = "IC"

    updateSinceUntilBuild = false

    setPlugins(
            "Kotlin",
            "org.intellij.plugins.markdown:191.7479.1",
            "com.intellij.testGuiFramework:0.9.44.1@nightly"
    )
}

tasks.withType<RunIdeTask> {
    jvmArgs("-Xmx1g")

    systemProperties(jbProperties<String>())
    args(execArguments())
}


val testsJar = tasks.create("guiTestJar", Jar::class) {
    group = "build"
    classifier = "tests"

    from(_sourceSets["test"].output)
    exclude("testData/*")
}

tasks.withType<PrepareSandboxTask> {
    from(testsJar) {
        exclude("testData/*")
        into("testGuiFramework/lib")
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))

    compile(project(":plugin"))
}

tasks.withType<Test> {
    environment["GUI_TEST_DATA_DIR"] = projectDir.absolutePath + "/src/test/resources/ide/ui/"

    systemProperties(jbProperties<Any>().also { it["idea.gui.tests.gradle.runner"] = true })

    include("**/*TestSuite*")

    testLogging {
        events("passed", "skipped", "failed")
    }
}
