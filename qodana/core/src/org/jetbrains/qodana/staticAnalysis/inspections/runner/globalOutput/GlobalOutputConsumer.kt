package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.io.FileUtil
import org.jdom.Element
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaToolResultDatabase
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import java.nio.file.Path

/**
 * Defines the consumer, that will be capable of taking ownership over XML files, produced
 * by global tools and containing issues, found in the code.
 *
 * If one consumer takes the ownership of file(s), those files will be inaccessible to other
 * consumers, even if the consumer decides to not process them.
 */
@ApiStatus.Internal
interface GlobalOutputConsumer {
  companion object {
    val EP_NAME: ExtensionPointName<GlobalOutputConsumer> =
      ExtensionPointName.create("org.intellij.qodana.globalOutputConsumer")

    /**
     * Runs registered global output providers. [DefaultGlobalOutputConsumer] consumes all the non-consumed files
     * by other providers.
     */
    internal suspend fun runConsumers(
      profileState: QodanaProfile.QodanaProfileState,
      paths: List<Path>,
      database: QodanaToolResultDatabase,
      project: Project,
      consumer: (List<Problem>, String) -> Unit
    ) {
      var nonProcessedPaths = paths.toList()
      for (provider in EP_NAME.extensionList) {
        val consumedPaths = provider.ownedFiles(nonProcessedPaths)
        if (consumedPaths.isNotEmpty()) {
          nonProcessedPaths = nonProcessedPaths.minus(consumedPaths.toSet())
          provider.consumeOwnedFiles(profileState, consumedPaths, database, project, consumer)
        }
      }
      if (nonProcessedPaths.any()) {
        DefaultGlobalOutputConsumer().consumeOwnedFiles(profileState, nonProcessedPaths, database, project, consumer)
      }
    }

    fun reportingInspectionAllowed(profileState: QodanaProfile.QodanaProfileState, inspectionId: String): Boolean {
      return profileState.stateByInspectionId[inspectionId]?.onConsumeProblem(inspectionId, null, null) == true
    }

    suspend fun consumeOutputXmlFile(path: Path, action: suspend (String, Element) -> Unit) {
      val file = path.toFile()
      val inspectionId = FileUtil.getNameWithoutExtension(file)
      val root = JDOMUtil.load(file)
      action(inspectionId, root)
    }
  }

  suspend fun consumeOwnedFiles(
    profileState: QodanaProfile.QodanaProfileState,
    paths: List<Path>,
    database: QodanaToolResultDatabase,
    project: Project,
    consumer: (List<Problem>, String) -> Unit
  )

  fun ownedFiles(paths: List<Path>): List<Path>
}