package org.jetbrains.qodana.cpp

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.configuration.HeadlessLogging
import com.intellij.openapi.project.guessProjectDir
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaTimeoutException
import kotlin.io.path.div
import kotlin.time.Duration

/**
 * Runs [block] with a timeout retrieved from [registryKey]. On timeout, logs diagnostics to idea.log
 * and prints an actionable message to stdout before throwing [QodanaTimeoutException].
 */
suspend fun withQodanaTimeout(
    project: Project,
    registryKey: String,
    timeout: Duration,
    block: suspend () -> Unit,
) {
    withTimeoutOrNull(timeout) {
        block()
    } ?: run {
        QodanaCppStartupManager.getInstanceAsync(project).logDiagnostics()
        println(userTimeoutMessage(project, registryKey))
        HeadlessLogging.logFatalError("$registryKey timeout reached")
        throw QodanaTimeoutException("$registryKey timeout reached")
    }
}

internal fun userTimeoutMessage(project: Project, registryKey: String): String {
    val ideaLogPath = PathManager.getLogDir() / "idea.log"
    val projectPath = project.guessProjectDir()?.path ?: "the current project directory"

    return """
    Qodana failed to configure the project: timeout reached.
      If you need more time for project configuration, increase `$registryKey`.
      If the project configuration failed but was not detected, please open an issue at https://jb.gg/qodana-issue and attach:
        - $ideaLogPath
        - A tarball of $projectPath or another example of reproduction.
    Detailed logs are available at $ideaLogPath
  """.trimIndent()
}
