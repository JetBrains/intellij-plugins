// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution

import com.intellij.coverage.CoverageExecutor
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.javascript.nodejs.execution.runConfiguration.NodeRunConfigurationExtensionsManager.Companion.getInstance
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.javascript.nodejs.util.NodePackage
import com.intellij.openapi.util.JDOMUtil
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
           myRunConfigurationExtensionsXml == that.myRunConfigurationExtensionsXml
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
    return result
  }

  companion object {
    private fun runConfigurationExtensionsToXml(runConfiguration: KarmaRunConfiguration): String {
      val element = Element("extensionsRoot")
      getInstance().writeExternal(runConfiguration, element)
      return JDOMUtil.write(element)
    }
  }
}