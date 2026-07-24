package org.jetbrains.qodana.staticAnalysis.packageChecker

import com.intellij.codeInspection.ex.InspectListener
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.packageChecker.model.exceptions.PackageCheckerHeadlessAnalysisException
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.qodana.registry.QodanaRegistry
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaAnalysisCancellationService
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaReportedFailureException

@ApiStatus.Internal
class PackageCheckerInspectListener : InspectListener {
  override fun inspectionFailed(toolId: String, throwable: Throwable, file: PsiFile?, project: Project) {
    if (throwable is PackageCheckerHeadlessAnalysisException &&
        QodanaRegistry.isPackageCheckerCancelOnFailureEnabled) {
      val reportedFailure = QodanaReportedFailureException.packageCheckerHeadlessFailure(
        throwable.message ?: throwable.toString(),
        throwable
      )
      project.service<QodanaAnalysisCancellationService>()
        .requestCancel(PACKAGE_CHECKER_QODANA_CANCELLATION_MESSAGE, reportedFailure)
    }
  }

  companion object {
    const val PACKAGE_CHECKER_QODANA_CANCELLATION_MESSAGE: String = "Package checker failed during Qodana analysis."
  }
}
