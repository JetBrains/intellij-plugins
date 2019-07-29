package tanvd.grazi

import org.gradle.api.Project

fun Project.gitBranch(): String {
    val builder = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
    builder.directory(rootProject.projectDir)
    val proc = builder.start()
    val branch = proc.inputStream.bufferedReader().readText()
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
        .filterKeys { it.toString().startsWith("idea") || it.toString().startsWith("jb") &&
                ((it as String) != "idea.home.path" && it != "jb.vmOptionsFile")
        }.mapKeys { it.key as String }.mapValues { it.value as Value }.toMutableMap()

fun execArguments() = System.getProperty("exec.args", "").split(",")
