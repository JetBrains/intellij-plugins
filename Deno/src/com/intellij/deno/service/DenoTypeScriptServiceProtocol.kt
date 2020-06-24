package com.intellij.deno.service

import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceAnswer
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptServiceStandardOutputProtocol
import com.intellij.lang.typescript.compiler.languageService.protocol.commands.TypeScriptServiceInitialStateObject
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer

class DenoTypeScriptServiceProtocol(project: Project,
                                    settings: TypeScriptCompilerSettings,
                                    readyConsumer: Consumer<*>,
                                    eventConsumer: Consumer<JSLanguageServiceAnswer>,
                                    serviceName: String,
                                    tsServicePath: String) :
  TypeScriptServiceStandardOutputProtocol(project, settings, readyConsumer, eventConsumer, serviceName, tsServicePath) {

  override fun createState(): TypeScriptServiceInitialStateObject {
    val state = super.createState()
    state.pluginName = "denoTypeScript"
    val pluginProbe = JSLanguageServiceUtil.getPluginDirectory(this::class.java, "deno-service/node_modules/typescript-deno-plugin").parentFile.parentFile.path
    state.pluginProbeLocations = state.pluginProbeLocations + LocalFilePath.create(pluginProbe)
    state.globalPlugins = arrayOf("typescript-deno-plugin", "ws-typescript-plugin")
    return state
  }
}