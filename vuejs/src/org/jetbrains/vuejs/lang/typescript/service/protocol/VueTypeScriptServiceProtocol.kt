// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript.service.protocol

import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptServiceInitialStateObject
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer

class VueTypeScriptServiceProtocol(project: Project,
                                   settings: TypeScriptCompilerSettings,
                                   readyConsumer: Consumer<*>,
                                   eventConsumer: Consumer<in JSLanguageServiceAnswer>,
                                   tsServicePath: String) :
  TypeScriptServiceStandardOutputProtocol(project, settings, readyConsumer, eventConsumer, "VueService", tsServicePath) {

  override fun createState(): TypeScriptServiceInitialStateObject {
    val state = super.createState()

    state.pluginName = "vueTypeScript"
    val pluginProbe = JSLanguageServiceUtil.getPluginDirectory(this::class.java, "vue-service/node_modules/ws-typescript-vue-plugin").parentFile.parentFile.path
    state.pluginProbeLocations = state.pluginProbeLocations + LocalFilePath.create(pluginProbe)
    state.globalPlugins = arrayOf("ws-typescript-vue-plugin")
    return state
  }
}
