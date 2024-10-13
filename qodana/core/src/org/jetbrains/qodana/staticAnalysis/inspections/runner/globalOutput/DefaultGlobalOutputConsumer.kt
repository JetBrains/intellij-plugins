package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.XmlProblem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput.GlobalOutputConsumer.Companion.consumeOutputXmlFile
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
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
        consumer(root.getChildren("problem").map { XmlProblem(it) }, inspectionId)
      }
    }
  }

  override fun ownedFiles(paths: List<Path>) = paths
}