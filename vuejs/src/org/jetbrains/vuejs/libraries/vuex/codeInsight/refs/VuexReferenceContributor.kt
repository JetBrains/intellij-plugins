// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight.refs

import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.VUEX_CALL_ARRAY_ITEM_LITERAL
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.VUEX_CALL_LITERAL_ARGUMENT
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.VUEX_CALL_OBJECT_PROPERTY_LITERAL
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.VUEX_DECORATOR_LITERAL_ARGUMENT
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.VUEX_INDEXED_ACCESS_LITERAL
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_CALL_ARGUMENT_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_CALL_ARRAY_OBJECT_ITEM_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_DECORATOR_ARGUMENT_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_INDEXED_ACCESS_REF_PROVIDER

class VuexReferenceContributor : PsiReferenceContributor() {

  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(VUEX_CALL_LITERAL_ARGUMENT,
                                        VUEX_CALL_ARGUMENT_REF_PROVIDER)
    registrar.registerReferenceProvider(VUEX_CALL_ARRAY_ITEM_LITERAL,
                                        VUEX_CALL_ARRAY_OBJECT_ITEM_REF_PROVIDER)
    registrar.registerReferenceProvider(VUEX_CALL_OBJECT_PROPERTY_LITERAL,
                                        VUEX_CALL_ARRAY_OBJECT_ITEM_REF_PROVIDER)
    registrar.registerReferenceProvider(VUEX_INDEXED_ACCESS_LITERAL,
                                        VUEX_INDEXED_ACCESS_REF_PROVIDER)
    registrar.registerReferenceProvider(VUEX_DECORATOR_LITERAL_ARGUMENT,
                                        VUEX_DECORATOR_ARGUMENT_REF_PROVIDER)
  }

  companion object {
  }
}
