package com.intellij.openRewrite.maven

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openRewrite.run.OpenRewriteExternalSystemBridge
import com.intellij.openRewrite.run.OpenRewriteRunConfiguration
import com.intellij.openapi.externalSystem.service.ui.project.path.ExternalProject
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType
import org.jetbrains.idea.maven.execution.MavenRunnerParameters
import org.jetbrains.idea.maven.execution.MavenRunnerSettings
import org.jetbrains.idea.maven.execution.run.configuration.MavenWorkingDirectoryInfo
import org.jetbrains.idea.maven.project.MavenProjectsManager
import org.jetbrains.idea.maven.utils.MavenUtil
import java.util.concurrent.CompletableFuture

internal class OpenRewriteMavenBridge : OpenRewriteExternalSystemBridge {
  override fun getDelegate(configuration: OpenRewriteRunConfiguration): RunConfiguration? {
    if (!MavenProjectsManager.getInstance(configuration.project).isMavenizedProject) return null

    val delegate = OpenRewriteMavenRunConfiguration(configuration)
    delegate.runnerParameters.workingDirPath = configuration.getExpandedWorkingDirectory() ?: configuration.project.basePath!!
    val vmOptions = configuration.vmOptions
    val env = configuration.envs
    if (!vmOptions.isNullOrBlank() || env.isNotEmpty() || !configuration.passParentEnv) {
      delegate.runnerSettings = MavenRunnerSettings().also {
        it.setVmOptions(vmOptions)
        it.environmentProperties = env
        it.isPassParentEnv = configuration.passParentEnv
      }
    }
    return delegate
  }

  override suspend fun collectExternalProjects(project: Project): List<ExternalProject> {
    return MavenWorkingDirectoryInfo(project).collectExternalProjects()
  }

  override fun hasBuildFile(directory: VirtualFile, project: Project): Boolean {
    return MavenUtil.streamPomFiles(project, directory).findAny().isPresent
  }

  override fun isBuildFile(module: Module, psiFile: PsiFile): Boolean = MavenUtil.isPomFile(module.project, psiFile.virtualFile)

  override fun adjustModule(module: Module): Module = module

  override fun isAvailable(project: Project): Boolean = MavenProjectsManager.getInstance(project).isMavenizedProject

  override fun installFile(project: Project, commandLine: String): Boolean {
    val result = CompletableFuture<Boolean>()
    val parameters = MavenRunnerParameters(false, "", null as String?, null, null)
    parameters.commandLine = commandLine
    MavenRunConfigurationType.runConfiguration(project, parameters, null, null, { descriptor: RunContentDescriptor ->
      val handler = descriptor.processHandler
      handler?.addProcessListener(object : ProcessAdapter() {
        override fun processTerminated(event: ProcessEvent) {
          result.complete(event.exitCode == 0)
        }
      })
    }, true)
    return result.get()
  }
}