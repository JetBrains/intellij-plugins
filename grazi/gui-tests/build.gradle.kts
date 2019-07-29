import org.jetbrains.intellij.tasks.*
import tanvd.grazi.*

intellij {
    pluginName = "Grazi"
    version = "2019.2"
    downloadSources = true
    type = "IС"

    updateSinceUntilBuild = false

    setPlugins(
            "Kotlin",
            "com.intellij.testGuiFramework:0.10.1@nightly"
    )

    alternativeIdePath = System.getProperty("idea.gui.test.alternativeIdePath")
}

tasks.withType<RunIdeTask> {
    jvmArgs("-Xmx2g")
    println(jbProperties<String>())
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
        into("Test GUI Framework/lib")
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
