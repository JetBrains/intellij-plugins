package org.jetbrains.qodana.cpp

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.lang.workspace.OCWorkspace
import com.jetbrains.cidr.project.workspace.CidrWorkspaceManager
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

private val log = logger<QodanaCppWorkflow>()

class QodanaCppWorkflow : QodanaWorkflowExtension {
    override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
        val configurations = OCWorkspace.getInstance(project).configurations
        if (configurations.isEmpty()) {
            val wsManager = CidrWorkspaceManager.getInstance(project)
            val workspaceStates = wsManager.workspaces.entries.joinToString(", ") { (ws, state) ->
                "${ws.javaClass.simpleName}=$state"
            }
            throw QodanaException(
                "Failed to calculate analysis scope from build configuration. " +
                        "No OC resolve configurations were detected. " +
                        "Workspace states: [$workspaceStates]. " +
                        "Check that the project has a supported build system (CMake, CompDB, Makefile, or Meson) " +
                        "and that the build files are valid."
            )
        }

        val fileCount = configurations.sumOf { it.sources.size }
        log.info("Analysis scope: $fileCount files across ${configurations.size} resolve configurations")
        println("Analysis scope: $fileCount files across ${configurations.size} resolve configurations")
    }
}