package org.jetbrains.qodana.run

import org.jetbrains.qodana.staticAnalysis.inspections.runner.FULL_SARIF_REPORT_NAME
import com.intellij.codeInspection.InspectionApplicationBase
import java.nio.file.Path

data class QodanaInIdeOutput(val reportGuid: String, val path: Path) {
  val sarifPath: Path
    get() = path.resolve(FULL_SARIF_REPORT_NAME)

  @Suppress("unused")
  val projectStructure: Path
    get() = path.resolve(InspectionApplicationBase.PROJECT_STRUCTURE_DIR)
}