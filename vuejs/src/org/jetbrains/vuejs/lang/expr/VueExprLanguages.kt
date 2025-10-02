// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr

import com.intellij.lang.DependentLanguage
import com.intellij.lang.Language
import com.intellij.lang.MetaLanguage
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JavaScriptSupportLoader

object VueJSLanguage :
  JSLanguageDialect(
    id = "VueJS",
    optionHolder = DialectOptionHolder.JS_WITH_JSX,
    baseLanguage = JavaScriptSupportLoader.ECMA_SCRIPT_6,
  ),
  DependentLanguage

object VueTSLanguage :
  JSLanguageDialect(
    id = "VueTS",
    optionHolder = DialectOptionHolder.TS,
    baseLanguage = JavaScriptSupportLoader.TYPESCRIPT,
  ),
  DependentLanguage

private val vueExprLanguages = setOf<JSLanguageDialect>(VueJSLanguage, VueTSLanguage)

fun isVueExprMetaLanguage(language: Language?): Boolean {
  return language != null && MetaLanguage.findInstance(VueExprMetaLanguage::class.java).matchesLanguage(language)
}

class VueExprMetaLanguage private constructor() : MetaLanguage("VueExpr") {

  override fun matchesLanguage(language: Language): Boolean {
    return vueExprLanguages.contains(language)
  }

  override fun getMatchingLanguages(): Collection<Language> {
    return vueExprLanguages
  }

}