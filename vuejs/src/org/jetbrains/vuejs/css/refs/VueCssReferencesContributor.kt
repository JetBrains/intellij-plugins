// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.css.refs

import com.intellij.javascript.web.css.refs.CssClassInJSLiteralOrIdentifierReferenceProvider
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.jetbrains.vuejs.codeInsight.attributes.VueCustomAttributeValueProvider.Companion.isVBindClassAttribute
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpression

class VueCssReferencesContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    CssClassInJSLiteralOrIdentifierReferenceProvider.register(registrar, VueJSLanguage.INSTANCE,
                                                              VueJSEmbeddedExpression::class.java, ::isVBindClassAttribute)
  }

}
