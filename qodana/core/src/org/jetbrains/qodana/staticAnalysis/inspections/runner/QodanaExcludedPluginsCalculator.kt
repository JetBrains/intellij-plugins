// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.application.ex.ApplicationManagerEx
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.runBlockingCancellable
import kotlinx.coroutines.delay
import java.io.File
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.exists
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
    if (args.size < 3) {
      printHelpAndExit()
    }
    val input = args[1]  //list of ids
    val output = args[2] //.dockerignore for update
    // fill disabled_plugins.txt
    @Suppress("UNUSED_VARIABLE") // disabled_plugins.txt is not modified by this script
    val disabledPluginsPath = if (args.size == 4) { args[3] } else ""
    try {
      val requiredIds = File(input).readLines()
      if (requiredIds.isEmpty()) {
        printHelpAndExit()
      }

      val (included, _) = calculateActual(requiredIds)
      val dockerIgnorePath = Paths.get(output)
      val baseFolder = dockerIgnorePath.parent.toAbsolutePath()

      val includedPaths = included.mapNotNull {
        val pathPresentation = it.pluginPath.toString()
        val path = Paths.get(pathPresentation).toAbsolutePath()
        if (path.exists()) "!${baseFolder.relativize(path)}" else null
      }
      dockerIgnorePath.toFile().appendText("\n" + includedPaths.joinToString("\n"))
      ApplicationManagerEx.getApplicationEx().exit(true, true)
    }
    catch (e: Throwable) {
      LOG.error(e)
      exitProcess(1)
    }
  }

  private fun printHelpAndExit() {
    println("Expected parameters: <path to file with list of included plugins> <.dockerginore path> <disabled_plugins.txt path>")
    exitProcess(1)
  }

  private fun calculateActual(included: List<String>): Pair<MutableSet<IdeaPluginDescriptor>, List<String>> {
    val plugins = PluginManagerCore.loadedPlugins
    val pluginIds = plugins.map { it.pluginId.idString }
    val processed = mutableSetOf<String>()
    val toProcess = LinkedList(included)
    val include = mutableSetOf<IdeaPluginDescriptor>()

    while (toProcess.isNotEmpty()) {
      val idString = toProcess.pop()
      val pluginId = PluginId.findId(idString)
      if (pluginId == null) {
        LOG.error("Can't find plugin id '$idString'")
        continue
      }
      val descriptor = PluginManagerCore.getPlugin(pluginId)
      if (descriptor == null) {
        LOG.error("Can't find plugin descriptor for id '$idString'")
        continue
      }
      toProcess += processPlugin(descriptor, include, processed)
    }

    println("=====INCLUDED=======")

    println(include.joinToString("\n") { it.pluginId.idString + " / " + it.pluginPath })

    val disabled = pluginIds - include.map { it.pluginId.idString }.toSet()

    println("=====DISABLED=======")

    println(disabled.sorted().joinToString("\n") { it })

    return include to disabled
  }

  private fun processPlugin(descriptor: IdeaPluginDescriptor, include: MutableSet<IdeaPluginDescriptor>, processed: MutableSet<String>): List<String> {
    val idString = descriptor.pluginId.idString
    if (processed.contains(idString)) return emptyList()
    processed.add(idString)
    include.add(descriptor)

    val required = if (descriptor is IdeaPluginDescriptorImpl) {
      descriptor.dependencies.plugins.mapNotNull { reference -> if (processed.contains(reference.id.idString)) null else reference.id.idString}
    } else emptyList()

    return descriptor.dependencies.mapNotNull {
      if (processed.contains(it.pluginId.idString) || it.isOptional) null else it.pluginId.idString
    } + required
  }
}