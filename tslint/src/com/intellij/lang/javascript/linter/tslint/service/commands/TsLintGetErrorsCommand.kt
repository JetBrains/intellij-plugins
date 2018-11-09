package com.intellij.lang.javascript.linter.tslint.service.commands

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceCommand
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand
import com.intellij.lang.javascript.service.protocol.LocalFilePath


class TsLintGetErrorsCommand(val fileName: LocalFilePath, val configPath: LocalFilePath?, val content: String) : JSLanguageServiceCommand, JSLanguageServiceSimpleCommand, JSLanguageServiceObject {

  override fun toSerializableObject(): JSLanguageServiceObject {
    return this
  }

  override fun getCommand(): String = "GetErrors"

}