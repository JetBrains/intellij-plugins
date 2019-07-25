import org.jetbrains.intellij.tasks.*
import tanvd.grazi.*

intellij {
    pluginName = "Grazi"
    version = "2019.1.3"
    downloadSources = true
    type = "IU"

    updateSinceUntilBuild = false

    setPlugins(
            "markdown",
            "Kotlin",
            "PythonCore:2019.1.191.6183.53",
            "org.rust.lang:0.2.98.2125-191",
            "nl.rubensten.texifyidea:0.6.6",
            "CSS",
            "JavaScriptLanguage",
            "properties",
            "com.intellij.testGuiFramework:0.9.44.1@nightly"
    )

    alternativeIdePath = System.getProperty("idea.gui.test.alternativeIdePath")
}

tasks.withType<RunIdeTask> {
    jvmArgs("-Xmx1g")

    systemProperties(jbProperties<String>())

    args(execArguments())
}

val guiTest = tasks.create("guiTest", Test::class) {
    group = "gui"

    environment["GUI_TEST_DATA_DIR"] = projectDir.absolutePath + "/src/test/resources/ide/ui/"
    systemProperties(jbProperties<Any>().also { it["idea.gui.tests.gradle.runner"] = true })
    include("**/*TestSuite*")

    useJUnit()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

val testsJar = tasks.create("guiTestJar", Jar::class) {
    group = "gui"
    classifier = "tests"

    from(_sourceSets["test"].output)
    exclude("testData/*")
}

tasks.withType<PrepareSandboxTask> {
    from(_sourceSets["test"].resources) {
        exclude("META-INF")
        into("testGuiFramework/lib")
    }

    from(_sourceSets["main"].resources) {
        exclude("META-INF")
        into("testGuiFramework/lib")
    }

    from(testsJar) {
        exclude("testData/*")
        into("testGuiFramework/lib")
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))

    compile(project(":plugin"))
}
