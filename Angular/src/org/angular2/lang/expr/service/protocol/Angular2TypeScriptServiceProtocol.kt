// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.service.protocol

import com.intellij.idea.AppMode
import com.intellij.lang.javascript.psi.util.JSPluginPathManager.getPluginResourceOrSource
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptServiceInitialStateObject
import com.intellij.openapi.project.Project
import java.io.File
import java.io.IOException
import java.util.function.Consumer

class Angular2TypeScriptServiceProtocol(project: Project,
                                        settings: TypeScriptCompilerSettings,
                                        readyConsumer: Consumer<*>,
                                        eventConsumer: Consumer<in JSLanguageServiceAnswer>,
                                        tsServicePath: String) :
  TypeScriptServiceStandardOutputProtocol(project, settings, readyConsumer, eventConsumer, "AngularService", tsServicePath) {

  override fun createState(): TypeScriptServiceInitialStateObject {
    val state = super.createState()
    state.pluginName = "angularTypeScript"
    return state
  }

  override fun getGlobalPlugins(): List<String> {
    val globalPlugins = super.getGlobalPlugins()
    return globalPlugins + "ws-typescript-angular-plugin"
  }

  override fun getProbeLocations(): Array<LocalFilePath> {
    val probeLocations = super.getProbeLocations()
    val pluginProbe = getAngularServicePluginLocation().parentFile.parentFile.path

    val element = LocalFilePath.create(pluginProbe) ?: return probeLocations
    return probeLocations + element
  }

  private fun getAngularServicePluginLocation(): File {
    try {
      return getPluginResourceOrSource(
        this::class.java,
        "angular-service/node_modules/ws-typescript-angular-plugin",
        if (AppMode.isDevServer()) "angular-plugin" else "Angular/gen")
    }
    catch (e: IOException) {
      throw RuntimeException(e)
    }
  }
}
