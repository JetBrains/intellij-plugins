package com.intellij.lang.javascript.linter.tslint.service.protocol

import com.intellij.lang.javascript.service.protocol.JSLanguageServiceInitialState


class TsLintLanguageServiceInitialState : JSLanguageServiceInitialState() {
  var tslintPackagePath: String? = null
  var additionalRootDirectory: String? = null
}
