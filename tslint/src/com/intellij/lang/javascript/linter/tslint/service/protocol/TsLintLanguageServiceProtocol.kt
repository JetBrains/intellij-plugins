package com.intellij.lang.javascript.linter.tslint.service.protocol

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration
import com.intellij.lang.javascript.service.JSLanguageServiceUtil
import com.intellij.lang.javascript.service.JSLanguageServiceUtil.getPluginDirectory
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceInitialState
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceNodeStdProtocolBase
import com.intellij.lang.javascript.service.protocol.LocalFilePath
import com.intellij.openapi.project.Project
import com.intellij.util.Consumer


class TsLintLanguageServiceProtocol(project: Project, readyConsumer: Consumer<*>) : JSLanguageServiceNodeStdProtocolBase(project,
                                                                                                                         readyConsumer) {

  override fun getInterpreter(): NodeJsInterpreter? {
    val extendedState = TsLintConfiguration.getInstance(myProject).extendedState
    return JSLanguageServiceUtil.getInterpreterIfValid(extendedState.state.interpreterRef.resolve(myProject))
  }

  override fun createState(): JSLanguageServiceInitialState {
    val result = TsLintLanguageServiceInitialState()
    val extendedState = TsLintConfiguration.getInstance(myProject).extendedState
    result.tslintPackagePath = extendedState.state.packagePath
    result.additionalRootDirectory = extendedState.state.rulesDirectory
    result.pluginName = "tslint"
    result.pluginPath = LocalFilePath.create(getPluginDirectory(this.javaClass, "js/languageService/tslint-plugin-provider.js").absolutePath)
    return result
  }

  override fun dispose() {
  }
}