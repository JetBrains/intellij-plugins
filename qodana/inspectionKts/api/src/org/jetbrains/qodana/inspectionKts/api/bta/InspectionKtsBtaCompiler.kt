package org.jetbrains.qodana.inspectionKts.api.bta

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.inspectionKts.InspectionKtsErrorLogManager
import org.jetbrains.qodana.inspectionKts.InspectionKtsCompilationResult
import java.nio.file.Path

interface InspectionKtsBtaCompiler {
  fun isEnabled(): Boolean = true

  suspend fun compile(
    project: Project,
    file: Path,
    errorLogger: InspectionKtsErrorLogManager.Logger,
    classLoader: ClassLoader,
  ): InspectionKtsCompilationResult

  companion object {
    val EP_NAME: ExtensionPointName<InspectionKtsBtaCompiler> =
      ExtensionPointName.create("org.jetbrains.qodana.inspectionKts.btaCompiler")
  }
}
