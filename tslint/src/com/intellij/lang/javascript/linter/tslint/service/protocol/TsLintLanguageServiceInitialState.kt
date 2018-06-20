package com.intellij.lang.javascript.linter.tslint.service.protocol

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceInitialState
import com.intellij.lang.javascript.service.protocol.LocalFilePath


class TsLintLanguageServiceInitialState : JSLanguageServiceInitialState() {
  var tslintPackagePath: LocalFilePath? = null
  var additionalRootDirectory: LocalFilePath? = null
}
