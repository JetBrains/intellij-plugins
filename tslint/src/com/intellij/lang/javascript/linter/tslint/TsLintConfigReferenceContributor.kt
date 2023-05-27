// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint

import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil.getSimpleReferencesPathProvider
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PlatformPatterns.psiFile
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.StandardPatterns.or
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar

const val extendsProperty: String = "extends"

class TsLintConfigReferenceContributor : PsiReferenceContributor() {

  private val CONFIG_PATTERN = psiFile(JsonFile::class.java).withName(TslintUtil.TSLINT_JSON)
  private val STRING_LITERAL_IN_CONFIG: PsiElementPattern.Capture<JsonStringLiteral> = psiElement(
    JsonStringLiteral::class.java).inFile(CONFIG_PATTERN)

  private val EXTENDS_ARRAY = STRING_LITERAL_IN_CONFIG.withSuperParent(1, psiElement(JsonArray::class.java)).
    withSuperParent(2, psiElement(JsonProperty::class.java).withName(extendsProperty))
  private val EXTENDS_STRING = STRING_LITERAL_IN_CONFIG.withSuperParent(1, psiElement(JsonProperty::class.java).withName(extendsProperty))

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(or(EXTENDS_ARRAY, EXTENDS_STRING), getSimpleReferencesPathProvider(arrayOf(".json")))
  }
}