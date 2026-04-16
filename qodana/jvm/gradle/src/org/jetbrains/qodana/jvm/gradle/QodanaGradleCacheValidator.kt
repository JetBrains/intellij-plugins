package org.jetbrains.qodana.jvm.gradle

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.extensions.ExtensionNotApplicableException
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.OrderRootType
import com.intellij.platform.backend.observation.Observation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import java.nio.file.Path
import kotlin.io.path.exists

internal class QodanaGradleCacheValidator :
  QodanaWorkflowExtension {
  init {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      throw ExtensionNotApplicableException.create()
    }
  }

  override suspend fun afterConfiguration(config: QodanaConfig, project: Project) {
    runGradleSyncIfJDKsAreMissing(project)
    reloadProjectJdkTable()
    Observation.awaitConfiguration(project)
  }

  private suspend fun reloadProjectJdkTable() {
    ProjectJdkTable.getInstance().allJdks.forEach {
      val sdkType = it.sdkType as? JavaSdk ?: return@forEach
      val hasEmptyClassesRoots = it.rootProvider.getUrls(OrderRootType.CLASSES).isEmpty()
      if (hasEmptyClassesRoots) {
        sdkType.setupSdkPaths(it)
      } else {
        withContext(Dispatchers.EDT) {
          ApplicationManager.getApplication().runWriteAction(Runnable { it.sdkModificator.commitChanges() })
        }
      }
    }
  }

  private fun runGradleSyncIfJDKsAreMissing(project: Project) {
    if (ProjectJdkTable.getInstance().allJdks.any {
        it.sdkType is JavaSdk && it.homePath?.let { p -> Path.of(p).exists() } == false
      }) {
      println("Missing JDK files, running Gradle sync...")
      ExternalSystemUtil.refreshProjects(ImportSpecBuilder(project, GradleConstants.SYSTEM_ID))
    }
  }
}