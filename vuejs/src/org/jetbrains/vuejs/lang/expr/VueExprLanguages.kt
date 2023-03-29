// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr

import com.intellij.lang.DependentLanguage
import com.intellij.lang.Language
import com.intellij.lang.MetaLanguage
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader

class VueJSLanguage : JSLanguageDialect("VueJS", DialectOptionHolder.JS_WITH_JSX,
                                        JavaScriptSupportLoader.ECMA_SCRIPT_6), DependentLanguage {

  companion object {
    val INSTANCE: VueJSLanguage = VueJSLanguage()
  }
}

class VueTSLanguage : JSLanguageDialect("VueTS", DialectOptionHolder.TS, JavaScriptSupportLoader.TYPESCRIPT), DependentLanguage {

  companion object {
    val INSTANCE: VueTSLanguage = VueTSLanguage()
  }
}

class VueExprMetaLanguage private constructor() : MetaLanguage("VueExpr") {

  override fun matchesLanguage(language: Language): Boolean {
    return languages.contains(language)
  }

  override fun getMatchingLanguages(): Collection<Language> {
    return languages
  }

  companion object {
    private val languages = setOf<JSLanguageDialect>(VueJSLanguage.INSTANCE, VueTSLanguage.INSTANCE)

    fun matches(language: Language?): Boolean {
      return language != null && Language.findInstance(VueExprMetaLanguage::class.java).matchesLanguage(language)
    }
  }
}