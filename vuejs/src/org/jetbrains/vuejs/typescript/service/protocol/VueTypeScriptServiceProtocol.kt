// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.typescript.service.protocol

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptServiceInitialStateObject
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer

const val vueService = "vueLanguageService"
const val vuePlugin = "vuePluginIDE"

class VueTypeScriptServiceProtocol(project: Project,
                                   settings: TypeScriptCompilerSettings,
                                   readyConsumer: Consumer<*>,
                                   eventConsumer: Consumer<in JSLanguageServiceAnswer>,
                                   tsServicePath: String) :
  TypeScriptServiceStandardOutputProtocol(project, settings, readyConsumer, eventConsumer, "VueService", tsServicePath) {

  override fun createState(): TypeScriptServiceInitialStateObject {
    val state = super.createState()
    //actually right now we don't need to override any properties for vue service
    state.pluginName = "vueTypeScript"
    return state
  }
}