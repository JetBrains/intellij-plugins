package org.jetbrains.qodana.inspectionKts.mcp

import com.intellij.mcpserver.impl.McpServerService
import com.jetbrains.qodana.sarif.model.SarifReport
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.QodanaRunContextFactory
import org.jetbrains.qodana.staticAnalysis.script.AnalysisKind
import org.jetbrains.qodana.staticAnalysis.script.QodanaScript
import org.jetbrains.qodana.staticAnalysis.script.QodanaScriptFactory
import org.jetbrains.qodana.staticAnalysis.script.QodanaScriptResult
import org.jetbrains.qodana.staticAnalysis.script.UnvalidatedParameters
import org.jetbrains.qodana.util.QodanaMessageReporter

internal class McpServerScriptFactory : QodanaScriptFactory {
  companion object {
    const val SCRIPT_NAME = "mcp-server"
  }

  override val scriptName: String get() = SCRIPT_NAME

  override fun parseParameters(parameters: String): Map<String, String> {
    if (parameters.isNotEmpty()) throw QodanaException("The '$SCRIPT_NAME' script does not take parameters")
    return emptyMap()
  }

  override fun createScript(
    config: QodanaConfig,
    messageReporter: QodanaMessageReporter,
    contextFactory: QodanaRunContextFactory,
    parameters: UnvalidatedParameters,
  ): QodanaScript = McpServerScript(messageReporter, contextFactory)
}

/**
 * Opens the project the same way a regular Qodana run does, but instead of running the analysis
 * starts the IDE MCP server and keeps the project open until the process is terminated.
 */
internal class McpServerScript(
  private val messageReporter: QodanaMessageReporter,
  private val runContextFactory: QodanaRunContextFactory,
) : QodanaScript {

  override val analysisKind: AnalysisKind
    get() = AnalysisKind.OTHER

  override suspend fun execute(report: SarifReport): QodanaScriptResult = supervisorScope {
    val runContext = runContextFactory.openRunContext(this)
    val mcpServer = McpServerService.getInstanceAsync()
    try {
      mcpServer.start()
      messageReporter.reportMessage(1, "Project '${runContext.project.name}' is open, analysis is skipped")
      // the port message is parsed by QodanaMcpServerIntegrationTest; keep the format in sync
      messageReporter.reportMessage(1, "MCP server is running on port ${mcpServer.port}:")
      messageReporter.reportMessage(1, "  - SSE endpoint: ${mcpServer.serverSseUrl}")
      messageReporter.reportMessage(1, "  - Streamable HTTP endpoint: ${mcpServer.serverStreamUrl}")
      messageReporter.reportMessage(1, "Terminate the process to stop the server and close the project")
      // The script never finishes on its own: it holds the project open until the qodana run is cancelled
      awaitCancellation()
    }
    finally {
      withContext(NonCancellable) {
        mcpServer.stop()
      }
    }
  }
}
