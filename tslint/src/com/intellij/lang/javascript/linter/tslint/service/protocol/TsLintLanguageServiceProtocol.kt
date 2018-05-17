package com.intellij.lang.javascript.linter.tslint.service.protocol

import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.JSLanguageServiceUtil.getPluginDirectory
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceInitialState
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceNodeStdProtocolBase
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer
import java.io.File


class TsLintLanguageServiceProtocol(project: Project, readyConsumer: Consumer<*>) : JSLanguageServiceNodeStdProtocolBase(project,
                                                                                                                         readyConsumer) {

  override fun getNodeInterpreter(): String? {
    val extendedState = TsLintConfiguration.getInstance(myProject).extendedState
    return JSLanguageServiceUtil.getInterpreterPathIfValid(extendedState.state.interpreterRef.resolve(myProject))
  }

  override fun createState(): JSLanguageServiceInitialState {
    val result = TsLintLanguageServiceInitialState()
    val extendedState = TsLintConfiguration.getInstance(myProject).extendedState
    result.tslintPackagePath = extendedState.state.packagePath
    result.additionalRootDirectory = extendedState.state.rulesDirectory
    result.pluginName = "tslint"
    val file = File(getPluginDirectory(this.javaClass, "js"), "languageService/tslint-plugin-provider")
    result.pluginPath = file.absolutePath
    return result
  }

  override fun dispose() {
  }
}