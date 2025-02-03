// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution

import com.intellij.coverage.CoverageExecutor
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.javascript.karma.scope.KarmaScopeKind
import com.intellij.javascript.nodejs.execution.runConfiguration.NodeRunConfigurationExtensionsManager.Companion.getInstance
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.javascript.testing.AngularCliConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.util.io.NioFiles
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.PathUtil
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.text.nullize
import org.jdom.Element

class KarmaServerSettings(private val executor: Executor,
                          val nodeInterpreter: NodeJsInterpreter,
                          val karmaPackage: NodePackage,
                          settings: KarmaRunSettings,
                          val runConfiguration: KarmaRunConfiguration) {
  val nodeOptions: String = settings.nodeOptions
  val configurationFilePath: String = settings.configPathSystemDependent
  val karmaOptions: String = settings.karmaOptions
  val workingDirectorySystemDependent: String = settings.workingDirectorySystemDependent
  val envData: EnvironmentVariablesData = settings.envData
  // Restart Karma server on extensions change, e.g. on adding a new Docker publish port
  private val myRunConfigurationExtensionsXml: String = runConfigurationExtensionsToXml(runConfiguration)
  // Restart Karma server when running tests from different Angular projects
  val angularProjectName: String? = detectAngularProjectName(settings)
  // Restart Karma server when running tests from different NX projects
  val nxProjectName: String? = detectNxProjectName(runConfiguration.project, settings)

  val isWithCoverage: Boolean
    get() = executor is CoverageExecutor

  val isDebug: Boolean
    get() = executor is DefaultDebugExecutor

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val that = other as KarmaServerSettings
    return executor.id == that.executor.id &&
           nodeInterpreter == that.nodeInterpreter &&
           nodeOptions == that.nodeOptions &&
           karmaPackage == that.karmaPackage &&
           configurationFilePath == that.configurationFilePath &&
           karmaOptions == that.karmaOptions &&
           workingDirectorySystemDependent == that.workingDirectorySystemDependent &&
           envData == that.envData &&
           myRunConfigurationExtensionsXml == that.myRunConfigurationExtensionsXml &&
           angularProjectName == that.angularProjectName &&
           nxProjectName == that.nxProjectName

  }

  override fun hashCode(): Int {
    var result = executor.id.hashCode()
    result = 31 * result + nodeInterpreter.hashCode()
    result = 31 * result + nodeOptions.hashCode()
    result = 31 * result + karmaPackage.hashCode()
    result = 31 * result + configurationFilePath.hashCode()
    result = 31 * result + karmaOptions.hashCode()
    result = 31 * result + workingDirectorySystemDependent.hashCode()
    result = 31 * result + envData.hashCode()
    result = 31 * result + myRunConfigurationExtensionsXml.hashCode()
    result = 31 * result + angularProjectName.hashCode()
    result = 31 * result + nxProjectName.hashCode()
    return result
  }

  companion object {
    private fun runConfigurationExtensionsToXml(runConfiguration: KarmaRunConfiguration): String {
      val element = Element("extensionsRoot")
      getInstance().writeExternal(runConfiguration, element)
      return JDOMUtil.write(element)
    }

    private fun detectAngularProjectName(settings: KarmaRunSettings): String? {
      if (ParametersListUtil.parse(settings.karmaOptions).contains("--project")) return null
      val workingDir = settings.workingDirectorySystemDependent.nullize(true)?.let {
        NioFiles.toPath(it)
      } ?: return null
      val config = AngularCliConfig.findProjectConfig(workingDir) ?: return null
      val contextFile = getContextFile(settings)
      return config.getProjectContainingFileOrDefault(contextFile)
    }

    private fun detectNxProjectName(project: Project, settings: KarmaRunSettings): String? {
      if (ParametersListUtil.parse(settings.karmaOptions).contains("--project")) return null
      val contextFile = getContextFile(settings) ?: return null
      val config = NxConfig.findNxConfig(project, contextFile) ?: return null
      return config.getProjectName()
    }

    private fun getContextFile(s: KarmaRunSettings) : VirtualFile? {
      val primaryFile = when (s.scopeKind) {
        KarmaScopeKind.TEST_FILE, KarmaScopeKind.SUITE, KarmaScopeKind.TEST -> s.testFileSystemDependentPath.toVirtualFile()
        else -> null
      }
      if (primaryFile != null) return primaryFile
      val configFileParentDir = PathUtil.getParentPath(s.configPathSystemDependent).toVirtualFile()
      val workingDir = s.workingDirectorySystemDependent.toVirtualFile()
      return getDeepestDirectory(configFileParentDir, workingDir)
    }

    private fun getDeepestDirectory(dir1: VirtualFile?, dir2: VirtualFile?): VirtualFile? {
      if (dir1 != null && dir2 != null) {
        if (VfsUtil.isAncestor(dir1, dir2, true)) return dir2
        if (VfsUtil.isAncestor(dir2, dir1, true)) return dir1
      }
      return dir1 ?: dir2
    }

    private fun String?.toVirtualFile(): VirtualFile? = this.nullize(true)?.let {
      LocalFileSystem.getInstance().findFileByPath(it)
    }
  }
}