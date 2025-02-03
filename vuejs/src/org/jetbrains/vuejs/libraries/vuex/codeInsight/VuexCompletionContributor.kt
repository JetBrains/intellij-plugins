// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.isVuexContext
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexArrayItemPattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexCallArgumentPattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexDecoratorArgumentPattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexDispatchCommitObjectArgPattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.vuexObjectPropertyValuePattern
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_ARRAY_ITEM_OR_OBJECT_PROP_VALUE_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_CALL_ARGUMENT_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_DECORATOR_ARGUMENT_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_DISPATCH_COMMIT_OBJECT_ARG_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexStoreSymbolStringReference

class VuexCompletionContributor : CompletionContributor() {

  init {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER)
      .inside(vuexCallArgumentPattern(JSReferenceExpression::class)),
           VuexCallReferenceCompletionProvider(VUEX_CALL_ARGUMENT_REF_PROVIDER))

    extend(CompletionType.BASIC, PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER)
      .inside(vuexArrayItemPattern(JSReferenceExpression::class)),
           VuexCallReferenceCompletionProvider(VUEX_ARRAY_ITEM_OR_OBJECT_PROP_VALUE_REF_PROVIDER))

    extend(CompletionType.BASIC, PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER)
      .inside(vuexObjectPropertyValuePattern(JSReferenceExpression::class)),
           VuexCallReferenceCompletionProvider(VUEX_ARRAY_ITEM_OR_OBJECT_PROP_VALUE_REF_PROVIDER))

    extend(CompletionType.BASIC, PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER)
      .inside(vuexDecoratorArgumentPattern(JSReferenceExpression::class)),
           VuexCallReferenceCompletionProvider(VUEX_DECORATOR_ARGUMENT_REF_PROVIDER))

    extend(CompletionType.BASIC, PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER)
      .inside(vuexDispatchCommitObjectArgPattern(JSReferenceExpression::class)),
           VuexCallReferenceCompletionProvider(VUEX_DISPATCH_COMMIT_OBJECT_ARG_REF_PROVIDER))
  }

  private class VuexCallReferenceCompletionProvider(private val provider: VuexJSLiteralReferenceProvider) : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val ref = parameters.position.containingFile.findReferenceAt(parameters.offset)
      if (ref is JSReferenceExpression && isVuexContext(ref)) {
        val settings = provider.getSettings(ref) ?: return
        result.addAllElements(VuexStoreSymbolStringReference.getLookupItems(
          ref, settings.baseNamespace, settings.symbolAccessor, "", true, settings.includeMembers))
      }
    }
  }
}
