package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaInspectionProfile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import java.nio.file.Path

data class QodanaRunContext(
  val project: Project,
  val loadedProfile: LoadedProfile,
  val scope: QodanaAnalysisScope,
  val qodanaProfile: QodanaProfile,
  val config: QodanaConfig,
  val runCoroutineScope: CoroutineScope,
  val messageReporter: QodanaMessageReporter,
  val changes: Map<String, Set<Int>>? = null,
)
val QodanaRunContext.baseProfile: QodanaInspectionProfile
  get() = loadedProfile.profile

val QodanaRunContext.projectPath: Path
  get() = config.projectPath
