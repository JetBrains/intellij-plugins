package org.intellij.qodana.rust

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.configuration.HeadlessLogging
import com.intellij.openapi.project.guessProjectDir
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaTimeoutException
import kotlin.io.path.div
import kotlin.time.Duration

/**
 * Runs [block] with [timeout]. On timeout, prints a user-facing message to stdout
 * with the [registryKey] for reconfiguration, and throws [QodanaTimeoutException].
 */
suspend fun withQodanaTimeout(
    project: Project,
    registryKey: String,
    timeout: Duration,
    block: suspend () -> Unit,
) {
    withTimeoutOrNull(timeout) { block() } ?: run {
        val ideaLogPath = PathManager.getLogDir() / "idea.log"
        val projectPath = project.guessProjectDir()?.path ?: "the current project directory"
        val message = buildString {
            appendLine("Qodana failed to configure the project: timeout reached.")
            appendLine("  If you need more time for project configuration, increase `$registryKey`.")
            appendLine("  If the project configuration failed but was not detected, please open an issue at https://jb.gg/qodana-issue and attach:")
            appendLine("    - $ideaLogPath")
            appendLine("    - A tarball of $projectPath or another example of reproduction.")
            append("Detailed logs are available at $ideaLogPath")
        }
        println(message)
        HeadlessLogging.logFatalError("$registryKey timeout reached")
        throw QodanaTimeoutException("$registryKey timeout reached")
    }
}
