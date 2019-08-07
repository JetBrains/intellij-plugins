package tanvd.grazi

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.exclude

fun Project.gitBranch(): String {
    val process = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD").directory(rootProject.projectDir).start()
    val branch = process.inputStream.bufferedReader().readText()
    return branch.split("\n").single { it.isNotBlank() }
}

val Project.channel: String
    get() {
        val branch = gitBranch()
        return when {
            branch.startsWith("stable") -> {
                "stable"
            }
            branch.startsWith("dev") -> {
                "dev"
            }
            branch.startsWith("master") -> {
                "nightly"
            }
            else -> {
                "feature"
            }
        }
    }

inline fun <reified Value> jbProperties() = System.getProperties()
        .filterKeys { it.toString().startsWith("idea") || it.toString().startsWith("jb") }
        .filterKeys { it.toString() !in setOf("idea.paths.selector", "jb.vmOptionsFile", "idea.version", "idea.home.path") }
        .mapKeys { it.key as String }.mapValues { it.value as Value }.toMutableMap()

fun execArguments() = System.getProperty("exec.args", "").split(",")
