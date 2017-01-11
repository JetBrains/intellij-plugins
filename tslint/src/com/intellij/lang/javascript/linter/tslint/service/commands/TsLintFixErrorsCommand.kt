package com.intellij.lang.javascript.linter.tslint.service.commands

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceCommand
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceObject
import com.intellij.lang.javascript.service.protocol.JSLanguageServiceSimpleCommand


class TsLintFixErrorsCommand(val fileName: String, val configPath: String) : JSLanguageServiceCommand, JSLanguageServiceSimpleCommand, JSLanguageServiceObject {

  override fun toSerializableObject(): JSLanguageServiceObject {
    return this
  }

  override fun getCommand(): String = "FixErrors"

}