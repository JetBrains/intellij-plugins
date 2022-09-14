// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.util.ProcessingContext

class VueScriptScopeCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    var ref = parameters.position.containingFile.findReferenceAt(parameters.offset)
    if (ref is PsiMultiReference) {
      ref = ref.references.find { it is JSReferenceExpressionImpl }
    }
    (ref as? JSReferenceExpressionImpl)
      ?.takeIf { it.qualifier == null }
      ?.let { VueScriptAdditionalScopeProvider.getAdditionalScopeSymbols(it) }
      ?.forEach {
        result.addElement(
          JSCompletionUtil.withJSLookupPriority(JSLookupUtilImpl.createLookupElement(it), JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY_EXOTIC))
      }
  }
}