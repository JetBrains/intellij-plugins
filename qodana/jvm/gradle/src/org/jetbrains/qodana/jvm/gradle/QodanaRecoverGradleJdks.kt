package org.jetbrains.qodana.jvm.gradle

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkProvider
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ExternalProjectSystemRegistry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.platform.backend.observation.Observation
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.qodana.jvm.java.QodanaConfigJdkService
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaConfig
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowCapabilities
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowCapability
import org.jetbrains.qodana.staticAnalysis.workflow.QodanaWorkflowExtension
import java.nio.file.Path
import kotlin.io.path.exists

val LOG: Logger = Logger.getInstance(QodanaRecoverGradleJdks::class.java)

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
      LOG.warn(
        "Gradle modules inherit a missing project SDK (${modulesWithMissingInheritedProjectSdk.joinToString { it.name }}), attempting recovery..."
      )
      getQodanaProjectJdk(project)?.also { applyProjectJdk(it, project) }
    }

    LOG.warn("Incomplete Gradle JDK state detected, running Gradle sync...")
    ExternalSystemUtil.refreshProjects(ImportSpecBuilder(project, GradleConstants.SYSTEM_ID))
    Observation.awaitConfiguration(project)
    recoverModulesWithMissingInheritedProjectSdk(project)
  }

  private fun hasBrokenRegisteredJavaJdks(): Boolean {
    return ProjectJdkTable.getInstance().allJdks.any {
      it.sdkType is JavaSdk && it.homePath?.let { p -> Path.of(p).exists() } == false
    }
  }

  private suspend fun recoverModulesWithMissingInheritedProjectSdk(project: Project) {
    val modulesToRecover = findGradleModulesWithMissingInheritedProjectSdk(project)
    if (modulesToRecover.isEmpty()) return

    val sdk = getQodanaProjectJdk(project) ?: getRegisteredInternalJdk(project) ?: run {
      LOG.warn(
        "Gradle modules still inherit a missing project SDK after sync (${modulesToRecover.joinToString { it.name }}), " +
        "but no fallback JDK is available - this will probably prevent the start of analysis"
      )
      return
    }

    LOG.warn(
      "Gradle modules still inherit a missing project SDK after sync (${modulesToRecover.joinToString { it.name }}), " +
      "assigning fallback project SDK '${sdk.name}'"
    )
    applyProjectJdk(sdk, project)
  }

  private fun findGradleModulesWithMissingInheritedProjectSdk(project: Project): Set<Module> {
    val externalProjectSystemRegistry = ExternalProjectSystemRegistry.getInstance()
    return ModuleManager.getInstance(project).modules.asSequence()
      .filter { externalProjectSystemRegistry.getExternalSource(it)?.id == GradleConstants.SYSTEM_ID.id }
      .filter { hasMissingInheritedProjectSdk(it) }
      .toSet()
  }

  private fun hasMissingInheritedProjectSdk(module: Module): Boolean {
    val rootManager = ModuleRootManager.getInstance(module)
    return rootManager.isSdkInherited && rootManager.sdk == null
  }

  private suspend fun applyProjectJdk(sdk: Sdk, project: Project) {
    val projectRootManager = ProjectRootManager.getInstance(project)
    if (projectRootManager.projectSdk == sdk) return

    LOG.info("Applying project JDK ${sdk.name} for Gradle recovery")
    edtWriteAction {
      val projectSdk = projectRootManager.projectSdk
      if (projectSdk == null || projectSdk != sdk) {
        projectRootManager.projectSdk = sdk
      }
    }
  }

  private suspend fun getQodanaProjectJdk(project: Project): Sdk? {
    val sdk = service<QodanaConfigJdkService>().getJdk() ?: return null
    val homePath = sdk.homePath ?: return sdk
    return ProjectJdkTable.getInstance(project).findJdk(sdk.name, sdk.sdkType.name)
           ?: ExternalSystemJdkUtil.lookupJdkByPath(project, homePath)
  }

  private fun getRegisteredInternalJdk(project: Project): Sdk? {
    val internalJdk = ExternalSystemJdkProvider.getInstance().internalJdk
    val homePath = internalJdk.homePath ?: return null
    return ExternalSystemJdkUtil.lookupJdkByPath(project, homePath)
  }
}
