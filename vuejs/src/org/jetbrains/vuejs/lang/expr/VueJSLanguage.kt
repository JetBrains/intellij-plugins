// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr

import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.parsing.JavaScriptParser
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.lang.expr.parser.VueJSParser

// This class is the original `VueJSLanguage` class,
// but it's renamed to allow instanceof check through deprecated class from 'language' package
@Deprecated("Public for internal purpose only!")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class _VueJSLanguage : JSLanguageDialect("VueJS", DialectOptionHolder.ECMA_6, JavaScriptSupportLoader.ECMA_SCRIPT_6) {
  override fun getFileExtension(): String {
    return "js"
  }

  override fun createParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
    return VueJSParser(builder)
  }
}

@Suppress("DEPRECATION")
class VueJSLanguage : org.jetbrains.vuejs.language.VueJSLanguage() {
  companion object {
    val INSTANCE: VueJSLanguage = VueJSLanguage()
  }
}
