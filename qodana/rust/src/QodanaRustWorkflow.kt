package org.intellij.qodana.rust

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaException
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import org.rust.cargo.project.model.CargoProject
import org.rust.cargo.project.model.cargoProjects

private val log = logger<QodanaRustWorkflow>()

class QodanaRustWorkflow : QodanaWorkflowExtension {
    override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
        val cargoProjects = project.cargoProjects
        if (!cargoProjects.hasAtLeastOneValidProject) {
            throw QodanaException("No Cargo projects were loaded. Check that the project has a valid Cargo.toml.")
        }

        val failedProjects = cargoProjects.allProjects.filter { it.mergedStatus !is CargoProject.UpdateStatus.UpToDate }
        if (failedProjects.isNotEmpty()) {
            val details = failedProjects.joinToString("\n") { p ->
                "  - ${p.presentableName}: ${p.mergedStatus.humanString}"
            }
            log.warn("Some Cargo projects failed to load (analysis continues):\n$details")
        }

        val packageCount = cargoProjects.allProjects.sumOf { it.workspace?.packages?.size ?: 0 }
        log.info("Analysis scope: $packageCount packages across ${cargoProjects.allProjects.size} Cargo projects")
        println("Analysis scope: $packageCount packages across ${cargoProjects.allProjects.size} Cargo projects")
    }
}
