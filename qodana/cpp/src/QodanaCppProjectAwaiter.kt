package org.jetbrains.qodana.cpp

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.configuration.HeadlessLogging
import com.intellij.platform.backend.observation.ActivityTracker
import com.intellij.util.PlatformUtils
import kotlinx.coroutines.CancellationException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import java.nio.file.Path

class QodanaCppProjectAwaiter : ActivityTracker {
    override val presentableName: String = "C++ build system configuration"

    private val log = logger<QodanaCppProjectAwaiter>()

    override suspend fun isInProgress(project: Project): Boolean {
        if (!PlatformUtils.isQodana()) return false
        return !QodanaCppStartupManager.getInstanceAsync(project).isStartupDone
    }

    override suspend fun awaitConfiguration(project: Project) {
        if (!PlatformUtils.isQodana()) return
        val manager = QodanaCppStartupManager.getInstanceAsync(project)
        try {
            withQodanaTimeout(project, "qd.cpp.startup.timeout.minutes", QodanaCppRegistry.startupTimeout) {
                manager.awaitStartup()
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            if (e is QodanaException) throw e  // already handled by withQodanaTimeout
            // Non-timeout failure (e.g. backend crash) — log and report
            manager.logDiagnostics()
            val ideaLog = Path.of(PathManager.getLogPath(), "idea.log")
            println("Qodana failed to configure the project: ${e.message ?: e.toString()}.")
            println("  Detailed logs are available at $ideaLog")
            log.error("C++ startup failed", e)
            HeadlessLogging.logFatalError(e.message ?: e.toString())
            throw e
        }
    }
}
