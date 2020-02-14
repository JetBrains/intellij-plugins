// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight.refs

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.VUEX_INDEXED_ACCESS_LITERAL
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexArrayItemPattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexCallArgumentPattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexDecoratorArgumentPattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexDispatchCommitObjectArgPattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexObjectPropertyValuePattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_ARRAY_ITEM_OR_OBJECT_PROP_VALUE_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_CALL_ARGUMENT_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_DECORATOR_ARGUMENT_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_DISPATCH_COMMIT_OBJECT_ARG_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_INDEXED_ACCESS_REF_PROVIDER

class VuexReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(vuexCallArgumentPattern(JSLiteralExpression::class),
                                        VUEX_CALL_ARGUMENT_REF_PROVIDER)
    registrar.registerReferenceProvider(vuexArrayItemPattern(JSLiteralExpression::class),
                                        VUEX_ARRAY_ITEM_OR_OBJECT_PROP_VALUE_REF_PROVIDER)
    registrar.registerReferenceProvider(vuexObjectPropertyValuePattern(JSLiteralExpression::class),
                                        VUEX_ARRAY_ITEM_OR_OBJECT_PROP_VALUE_REF_PROVIDER)
    registrar.registerReferenceProvider(vuexDecoratorArgumentPattern(JSLiteralExpression::class),
                                        VUEX_DECORATOR_ARGUMENT_REF_PROVIDER)
    registrar.registerReferenceProvider(vuexDispatchCommitObjectArgPattern(JSLiteralExpression::class),
                                        VUEX_DISPATCH_COMMIT_OBJECT_ARG_REF_PROVIDER)

    registrar.registerReferenceProvider(VUEX_INDEXED_ACCESS_LITERAL,
                                        VUEX_INDEXED_ACCESS_REF_PROVIDER)
  }
}
