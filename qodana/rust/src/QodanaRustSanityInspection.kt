package org.intellij.qodana.rust

import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.GlobalInspectionContext
import com.intellij.codeInspection.GlobalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptionsProcessor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.util.PlatformUtils
import org.rust.cargo.project.model.CargoProject
import org.rust.cargo.project.model.cargoProjects

/**
 * Reports Cargo projects that failed to load during Qodana analysis.
 *
 * When `fetchBuildScriptsInfo` or other cargo operations fail, the analysis continues
 * with a degraded project model. This inspection surfaces those failures as findings
 * so users are aware that results may be incomplete.
 */
class QodanaRustSanityInspection : GlobalInspectionTool() {

    override fun runInspection(
        scope: AnalysisScope,
        manager: InspectionManager,
        globalContext: GlobalInspectionContext,
        problemDescriptionsProcessor: ProblemDescriptionsProcessor,
    ) {
        val project = globalContext.project
        if (!PlatformUtils.isQodana()) return

        val failedProjects = project.cargoProjects.allProjects.mapNotNull { p ->
            val status = p.mergedStatus as? CargoProject.UpdateStatus.UpdateFailed ?: return@mapNotNull null
            p to status
        }

        for ((cargoProject, status) in failedProjects) {
            val detail = ": ${status.reason}" + (status.message?.let { " ($it)" } ?: "")

            val message = QodanaRustBundle.message(
                "inspection.rust.sanity.cargo.project.failed",
                cargoProject.presentableName, detail)

            val vf = LocalFileSystem.getInstance().findFileByNioFile(cargoProject.manifest)
            val psiFile = vf?.let { PsiManager.getInstance(project).findFile(it) } ?: continue

            // Severity follows the inspection profile (defaulting to level in plugin XML)
            val problem = manager.createProblemDescriptor(psiFile, message, false, emptyArray(), ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
            problemDescriptionsProcessor.addProblemElement(globalContext.refManager.getReference(psiFile), problem)
        }
    }
}
