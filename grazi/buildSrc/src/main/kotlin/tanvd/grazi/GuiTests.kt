package tanvd.grazi

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.intellij.tasks.PrepareSandboxTask

fun Project.setupGuiTests() {
    val guiTest = tasks.create("guiTest", Test::class) {
        group = "gui"

        environment["GUI_TEST_DATA_DIR"] = projectDir.absolutePath + "/src/test/resources/ide/ui/"
        systemProperties(jbProperties<Any>().also { it["idea.gui.tests.gradle.runner"] = true })
        include("**/*TestSuite*")
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

    tasks["compileKotlin"].dependsOn(tasks["clean"])

    tasks["clean"].apply {
        outputs.upToDateWhen { false }
    }
}
