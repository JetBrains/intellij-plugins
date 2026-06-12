package com.intellij.openRewrite.gradle

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openRewrite.run.OpenRewriteExternalSystemBridge
import com.intellij.openRewrite.run.OpenRewriteRunConfiguration
import com.intellij.openapi.externalSystem.ExternalSystemModulePropertyManager
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.service.ui.project.path.ExternalProject
import com.intellij.openapi.externalSystem.service.ui.project.path.ExternalSystemWorkingDirectoryInfo
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.gradle.util.GradleConstants
import org.jetbrains.plugins.gradle.util.GradleUtil
import java.io.File

internal class OpenRewriteGradleBridge : OpenRewriteExternalSystemBridge {
  override fun getDelegate(configuration: OpenRewriteRunConfiguration): RunConfiguration? {
    val workingDirectoryPath = configuration.getExpandedWorkingDirectory()
    val workingDirectory = workingDirectoryPath?.let { VfsUtil.findFileByIoFile(File(it), false) } ?: return null
    if (workingDirectory.findChild(GradleConstants.DEFAULT_SCRIPT_NAME) == null &&
        workingDirectory.findChild(GradleConstants.KOTLIN_DSL_SCRIPT_NAME) == null) {
      return null
    }
    val delegate = OpenRewriteGradleRunConfiguration(configuration)
    delegate.settings.externalProjectPath = workingDirectoryPath
    delegate.settings.taskNames = listOf(getTaskName(configuration))
    delegate.settings.vmOptions = configuration.vmOptions
    delegate.settings.env =
      if (configuration.envs.isEmpty()) emptyMap() else LinkedHashMap(configuration.envs)
    delegate.settings.isPassParentEnvs = configuration.passParentEnv
    return delegate
  }

  private fun getTaskName(configuration: OpenRewriteRunConfiguration): String = if (configuration.dryRun) "rewriteDryRun" else "rewriteRun"

  override suspend fun collectExternalProjects(project: Project): List<ExternalProject> {
    return ExternalSystemWorkingDirectoryInfo(project, GradleConstants.SYSTEM_ID).collectExternalProjects()
  }

  override fun hasBuildFile(directory: VirtualFile, project: Project): Boolean {
    return directory.getChildren().any {
      GradleConstants.DEFAULT_SCRIPT_NAME == it.name ||
      GradleConstants.KOTLIN_DSL_SCRIPT_NAME == it.name
    }
  }

  override fun isBuildFile(module: Module, psiFile: PsiFile): Boolean {
    val virtualFile = psiFile.virtualFile ?: return false
    val parent = virtualFile.parent ?: return false

    if (parent.path != ExternalSystemModulePropertyManager.getInstance(module).getLinkedProjectPath()) {
      return false
    }
    val fileName = virtualFile.name
    return GradleConstants.DEFAULT_SCRIPT_NAME == fileName || GradleConstants.KOTLIN_DSL_SCRIPT_NAME == fileName
  }

  override fun adjustModule(module: Module): Module {
    val dataNode = GradleUtil.findGradleModuleData(module) ?: return module
    val mainSourceSetModuleId = dataNode.data.id + ":main"
    return ModuleManager.getInstance(module.project).modules.find {
      mainSourceSetModuleId == ExternalSystemApiUtil.getExternalProjectId(it)
    } ?: module
  }

  override fun isAvailable(project: Project): Boolean {
    return ProjectDataManager.getInstance().getExternalProjectsData(project, GradleConstants.SYSTEM_ID).isNotEmpty()
  }

  override fun installFile(project: Project, commandLine: String): Boolean {
    return false
  }
}