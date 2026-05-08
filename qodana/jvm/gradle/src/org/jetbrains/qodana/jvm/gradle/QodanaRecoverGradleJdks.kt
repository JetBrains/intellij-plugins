package org.jetbrains.qodana.jvm.gradle

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.ExternalProjectSystemRegistry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.platform.backend.observation.Observation
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.qodana.jvm.java.QodanaConfigJdkService
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.inspections.runner.ConsoleLog
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowCapabilities
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowCapability
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import java.nio.file.Path
import kotlin.io.path.exists

internal class QodanaRecoverGradleJdks :
  QodanaWorkflowExtension {

  override val provides: Set<QodanaWorkflowCapability>
    get() = setOf(QodanaWorkflowCapabilities.JdkRecovery)

  override suspend fun configureForQodana(config: QodanaConfig, project: Project) {
    recoverGradleJdkState(project)
  }

  private suspend fun recoverGradleJdkState(project: Project) {
    val modulesWithMissingInheritedProjectSdk = findGradleModulesWithMissingInheritedProjectSdk(project)
    if (!hasBrokenRegisteredJavaJdks() && modulesWithMissingInheritedProjectSdk.isEmpty()) return

    if (modulesWithMissingInheritedProjectSdk.isNotEmpty()) {
      ConsoleLog.warn(
        "Gradle modules inherit a missing project SDK (${modulesWithMissingInheritedProjectSdk.joinToString()}), attempting recovery..."
      )
      applyQodanaProjectJdk(project)
    }

    ConsoleLog.warn("Incomplete Gradle JDK state detected, running Gradle sync...")
    ExternalSystemUtil.refreshProjects(ImportSpecBuilder(project, GradleConstants.SYSTEM_ID))
    Observation.awaitConfiguration(project)
    applyQodanaProjectJdk(project)
  }

  private fun hasBrokenRegisteredJavaJdks(): Boolean {
    return ProjectJdkTable.getInstance().allJdks.any {
      it.sdkType is JavaSdk && it.homePath?.let { p -> Path.of(p).exists() } == false
    }
  }

  private fun findGradleModulesWithMissingInheritedProjectSdk(project: Project): List<String> {
    val externalProjectSystemRegistry = ExternalProjectSystemRegistry.getInstance()
    return ModuleManager.getInstance(project).modules.asSequence()
      .filter { externalProjectSystemRegistry.getExternalSource(it)?.id == GradleConstants.SYSTEM_ID.id }
      .filter { ModuleRootManager.getInstance(it).isSdkInherited }
      .filter { ModuleRootManager.getInstance(it).sdk == null }
      .map { it.name }
      .sorted()
      .toList()
  }

  private suspend fun applyQodanaProjectJdk(project: Project) {
    if (ProjectRootManager.getInstance(project).projectSdk != null) return

    val sdk = service<QodanaConfigJdkService>().getJdk() ?: return
    ConsoleLog.info("Applying Qodana-configured project JDK ${sdk.name} for Gradle recovery")
    edtWriteAction {
      if (ProjectRootManager.getInstance(project).projectSdk == null) {
        ProjectRootManager.getInstance(project).projectSdk = sdk
      }
    }
  }
}
