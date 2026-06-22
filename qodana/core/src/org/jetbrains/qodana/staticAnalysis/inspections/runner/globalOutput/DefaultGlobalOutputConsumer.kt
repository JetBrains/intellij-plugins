package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

import com.intellij.codeInspection.ex.GlobalInspectionContextBase
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.XmlProblem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer.Companion.consumeOutputXmlFile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintDebugLog
import java.nio.file.Path

/**
 * Default consumer which will be executed at the very end of registered consumers
 * and will process all the files left.
 */
class DefaultGlobalOutputConsumer : GlobalOutputConsumer {
  override suspend fun consumeOwnedFiles(
    profileState: QodanaProfile.QodanaProfileState,
    paths: List<Path>,
    database: QodanaToolResultDatabase,
    project: Project,
    consumer: (List<Problem>, String) -> Unit
  ) {
    for (path in paths) {
      consumeOutputXmlFile(path) { inspectionId, root ->
        val problems = root.getChildren("problem")
        StructuralFingerprintDebugLog.log(
          "DefaultGlobalOutputConsumer.consumeOwnedFiles path=$path, inspection=$inspectionId, " +
          "localTool=${root.getAttributeValue(GlobalInspectionContextBase.LOCAL_TOOL_ATTRIBUTE)}, problems=${problems.size}"
        )
        consumer(problems.map { XmlProblem(it) }, inspectionId)
      }
    }
  }

  override fun ownedFiles(paths: List<Path>) = paths
}