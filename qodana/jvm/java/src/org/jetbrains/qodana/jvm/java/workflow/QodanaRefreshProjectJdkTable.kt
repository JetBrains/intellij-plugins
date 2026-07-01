package org.jetbrains.qodana.jvm.java.workflow

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.OrderRootType
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowCapabilities
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowCapability
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension

internal class QodanaRefreshProjectJdkTable : QodanaWorkflowExtension {

  override val dependsOn: Set<QodanaWorkflowCapability>
    get() = setOf(QodanaWorkflowCapabilities.JdkRecovery)

  override suspend fun configureForQodana(config: QodanaConfig, project: Project) {
    refreshProjectJdkTable()
  }

  private suspend fun refreshProjectJdkTable() {
    ProjectJdkTable.getInstance().allJdks.forEach {
      val sdkType = it.sdkType as? JavaSdk ?: return@forEach

      val hasEmptyClassesRoots = it.rootProvider.getFiles(OrderRootType.CLASSES).isEmpty()
      if (hasEmptyClassesRoots) {
        sdkType.setupSdkPaths(it)
      }
      else {
        edtWriteAction { it.sdkModificator.commitChanges() }
      }
    }
  }
}
