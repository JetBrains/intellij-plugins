// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.codeInsight

import com.intellij.codeInsight.completion.*
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.libraries.vuex.codeInsight.VuexPatterns.VUEX_CALL_REFERENCE_ARGUMENT
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexJSLiteralReferenceProvider.Companion.VUEX_CALL_ARGUMENT_REF_PROVIDER
import org.jetbrains.vuejs.libraries.vuex.codeInsight.refs.VuexStoreSymbolStringReference

class VuexCompletionContributor : CompletionContributor() {

  init {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER)
      .inside(VUEX_CALL_REFERENCE_ARGUMENT), VuexCallReferenceCompletionProvider())
  }

  private class VuexCallReferenceCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
      val ref = parameters.position.containingFile.findReferenceAt(parameters.offset)
      if (ref is JSReferenceExpression && isVueContext(ref)) {
        val settings = VUEX_CALL_ARGUMENT_REF_PROVIDER.getSettings(ref) ?: return
        result.addAllElements(VuexStoreSymbolStringReference.getLookupItems(
          ref, settings.baseNamespaceProvider, settings.symbolAccessor, "", true))
      }
    }
  }
}
