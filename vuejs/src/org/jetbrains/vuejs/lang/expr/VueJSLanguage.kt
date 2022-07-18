// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr

import com.intellij.lang.DependentLanguage
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader

class VueJSLanguage : JSLanguageDialect("VueJS", DialectOptionHolder.JS_WITH_JSX, JavaScriptSupportLoader.ECMA_SCRIPT_6), DependentLanguage {

  companion object {
    val INSTANCE: VueJSLanguage = VueJSLanguage()
  }
}
