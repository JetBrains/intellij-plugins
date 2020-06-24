// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.completion.JSLookupUtilImpl
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.util.ProcessingContext
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.model.VueModelManager

class VueThisInstanceCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    var ref = parameters.position.containingFile.findReferenceAt(parameters.offset)
    if (ref is PsiMultiReference) {
      ref = ref.references.find { it is JSReferenceExpressionImpl }
    }
    ref.castSafelyTo<JSReferenceExpressionImpl>()
      ?.qualifier
      ?.castSafelyTo<JSThisExpression>()
      ?.let { VueModelManager.findComponentForThisResolve(it) }
      ?.thisType
      ?.asRecordType()
      ?.properties
      ?.asSequence()
      ?.mapNotNull {
        JSLookupUtilImpl.createPrioritizedLookupItem(it.memberSource.singleElement, it.memberName, JSLookupPriority.NESTING_LEVEL_1)
      }
      ?.forEach(result::addElement)
  }
}