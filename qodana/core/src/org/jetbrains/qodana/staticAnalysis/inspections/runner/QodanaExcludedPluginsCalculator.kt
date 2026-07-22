// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.ide.plugins.ContentModuleDescriptor
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl
import com.intellij.ide.plugins.PluginMainDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.ide.plugins.PluginModuleDescriptor
import com.intellij.ide.plugins.PluginModuleId
import com.intellij.ide.plugins.PluginSet
import com.intellij.ide.plugins.ResolvedPluginSet
import com.intellij.ide.plugins.getMainDescriptor
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.runBlockingCancellable
import kotlinx.coroutines.delay
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.ArrayDeque
import kotlin.io.path.appendText
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.system.exitProcess


private val LOG: Logger
  get() = logger<QodanaExcludedPluginsCalculator>()

class QodanaExcludedPluginsCalculator : ApplicationStarter {
  override val requiredModality: Int
    get() = ApplicationStarter.NOT_IN_EDT

  override fun main(args: List<String>) {
    // Intellij may fail without some warmup
    runBlockingCancellable {
      delay(30000)
    }
    if (args.size < 4) {
      printHelpAndExit()
    }
    val input = args[1]  //list of ids
    val output = args[2] //.dockerignore for update
    val disabledPluginMacFile = args[3] // fill disabled_plugins_mac.txt
    try {
      val requiredIds = Paths.get(input).readLines()
      if (requiredIds.isEmpty()) {
        printHelpAndExit()
      }

      val (included, disabledIds) = calculateActual(getRequiredPluginIds(requiredIds))
      val dockerIgnorePath = Paths.get(output)
      val baseFolder = dockerIgnorePath.parent.toAbsolutePath()

      val includedPaths = included.mapNotNull {
        val pathPresentation = it.pluginPath.toString()
        val path = Paths.get(pathPresentation).toAbsolutePath()
        if (path.exists()) "!${baseFolder.relativize(path)}" else null
      }
      dockerIgnorePath.appendText("\n" + includedPaths.joinToString("\n"))

      val disabledPluginMacPath = Paths.get(disabledPluginMacFile)
      val predefinedDisabled = Files.readString(disabledPluginMacPath).trim()
      val disablePluginsText = disabledIds.joinToString("\n") + "\n" + predefinedDisabled

      Files.writeString(disabledPluginMacPath, disablePluginsText, StandardOpenOption.TRUNCATE_EXISTING)
      ApplicationManagerEx.getApplicationEx().exit(true, true)
    }
    catch (@Suppress("IncorrectCancellationExceptionHandling") e: Throwable) {
      // Never hand a control-flow exception to LOG.error: the platform logger rethrows it, which would skip
      // the exitProcess below and hang this ApplicationStarter on non-daemon threads (same defect as QD-15440).
      if (!Logger.shouldRethrow(e)) LOG.error(e)
      exitProcess(1)
    }
  }

  private fun printHelpAndExit() {
    println("Expected parameters: <path to file with list of included plugins> <.dockerginore path> <disabled_plugins.txt path>")
    exitProcess(1)
  }

  private fun calculateActual(included: List<String>): Pair<Set<PluginMainDescriptor>, List<String>> {
    val pluginSet = PluginManagerCore.getPluginSet()
    val resolvedPluginSet = pluginSet.resolvedPluginSet ?: error("Resolved plugin set is not available")
    val pluginIds = pluginSet.enabledPlugins.map { it.pluginId.idString }
    val toProcess = ArrayDeque<IdeaPluginDescriptorImpl>()
    for (idString in included) {
      val descriptor = pluginSet.findIncludedDescriptor(idString)
      if (descriptor == null) {
        LOG.error("Can't find enabled plugin or content module for id '$idString'")
        continue
      }
      toProcess.add(descriptor)
    }

    val processed = mutableSetOf<IdeaPluginDescriptorImpl>()
    val include = linkedSetOf<PluginMainDescriptor>()

    while (toProcess.isNotEmpty()) {
      val descriptor = toProcess.removeFirst()
      if (!processed.add(descriptor)) continue

      include.add(descriptor.getMainDescriptor())
      if (descriptor is PluginMainDescriptor) {
        for (contentModule in descriptor.contentModules) {
          if (contentModule.moduleLoadingRule.required && resolvedPluginSet.isResolved(contentModule)) {
            toProcess.add(contentModule)
          }
        }
      }
      if (descriptor is ContentModuleDescriptor) {
        toProcess.add(descriptor.parent)
      }
      for (dependency in resolvedPluginSet.getDirectResolvedDependencies(descriptor)) {
        toProcess.add(dependency)
      }
    }

    println("=====INCLUDED=======")

    println(include.joinToString("\n") { it.pluginId.idString + " / " + it.pluginPath })

    val disabled = pluginIds - include.map { it.pluginId.idString }.toSet()

    println("=====DISABLED=======")

    println(disabled.sorted().joinToString("\n") { it })

    return include to disabled
  }

  private fun PluginSet.findIncludedDescriptor(idString: String): PluginModuleDescriptor? {
    val pluginId = PluginId.getId(idString)
    return findEnabledPlugin(pluginId)
           ?: findEnabledModule(PluginModuleId(idString, PluginModuleId.JETBRAINS_NAMESPACE))
           ?: getEnabledModules().asSequence()
             .filterIsInstance<ContentModuleDescriptor>()
             .firstOrNull { it.moduleId.name == idString }
  }

  private fun ResolvedPluginSet.isResolved(descriptor: IdeaPluginDescriptorImpl): Boolean {
    return descriptor in sortedResolvedDescriptors
  }
}

internal fun getRequiredPluginIds(
  included: List<String>,
  essentialPluginIds: List<PluginId> = ApplicationInfoEx.getInstanceEx().essentialPluginIds,
): List<String> = (included + essentialPluginIds.map { it.idString }).distinct()
