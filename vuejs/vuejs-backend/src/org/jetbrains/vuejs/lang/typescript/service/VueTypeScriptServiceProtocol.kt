package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.idea.AppMode
import com.intellij.lang.javascript.psi.util.JSPluginPathManager.getPluginResource
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.languageService.frameworks.DownloadableTypeScriptServicePlugin
import com.intellij.lang.typescript.compiler.languageService.frameworks.PluggableTypeScriptServiceProtocol
import com.intellij.openapi.project.Project
import java.io.IOException
import java.nio.file.Path
import java.util.function.Consumer

internal class VueTypeScriptServiceProtocol(
  project: Project,
  settings: TypeScriptCompilerSettings,
  eventConsumer: Consumer<in JSLanguageServiceAnswer>,
  serviceName: String,
  tsServicePath: String,
  servicePlugin: DownloadableTypeScriptServicePlugin,
) : PluggableTypeScriptServiceProtocol(
  project = project,
  settings = settings,
  eventConsumer = eventConsumer,
  serviceName = serviceName,
  tsServicePath = tsServicePath,
  servicePlugin = servicePlugin,
) {
  override fun getGlobalPlugins(): List<String> {
    val globalPlugins = super.getGlobalPlugins()
    return globalPlugins + "ws-typescript-vue-plugin"
  }

  override fun getProbeLocations(): Array<LocalFilePath> {
    val probeLocations = super.getProbeLocations()
    val pluginProbe = getVueServicePluginLocation().parent?.parent 
                      ?: return probeLocations

    val element = LocalFilePath.create(pluginProbe.toString())
    return probeLocations + element
  }

  private fun getVueServicePluginLocation(): Path =
    try {
      getPluginResource(
        this::class.java,
        "vue-service/node_modules/ws-typescript-vue-plugin",
        if (AppMode.isRunningFromDevBuild()) "vuejs" else "vuejs/gen-resources")
    }
    catch (e: IOException) {
      throw RuntimeException(e)
    }
}
