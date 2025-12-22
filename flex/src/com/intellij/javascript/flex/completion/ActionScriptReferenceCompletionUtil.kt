// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.javascript.flex.resolve.ActionScriptQualifiedItemProcessor
import com.intellij.javascript.flex.resolve.ActionScriptSinkResolveProcessor
import com.intellij.javascript.flex.resolve.ActionScriptVariantsProcessor
import com.intellij.lang.javascript.JSConditionalCompilationDefinitionsProvider
import com.intellij.lang.javascript.completion.JSCompletionUtil
import com.intellij.lang.javascript.completion.JSLookupPriority
import com.intellij.lang.javascript.psi.JSElvisOwner
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecmal4.JSSuperExpression
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor
import com.intellij.lang.javascript.psi.resolve.CompletionResultSink
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

object ActionScriptReferenceCompletionUtil {
  /** populates returned variants itself  */
  @JvmStatic
  fun calcDefaultVariants(
    expression: JSElvisOwner,
    containingFile: PsiFile,
    pushedSmartVariants: Set<String>,
    parameters: CompletionParameters,
    resultSet: CompletionResultSet
  ): Collection<LookupElement> {

    assert(expression is JSReferenceExpression || expression is JSIndexedPropertyAccessExpression)

    val qualifier = when (expression) {
      is JSIndexedPropertyAccessExpression -> expression.getQualifier()
      else -> (expression as JSReferenceExpression).getQualifier()
    }

    val sink =
      CompletionResultSink(expression, resultSet.prefixMatcher, pushedSmartVariants, !parameters.isExtendedCompletion, false)

    val parent = expression.getParent()
    if (qualifier == null || qualifier is JSSuperExpression) {
      if (expression is JSReferenceExpression && JSResolveUtil.isSelfReference(parent, expression)) {
        return mutableListOf() // Prevent Rulezz to appear
      }

      val localProcessor = ActionScriptSinkResolveProcessor(sink)

      JSReferenceExpressionImpl.doProcessLocalDeclarations(expression, qualifier, localProcessor, true, true, null)
      if (sink.isOverflow) {
        JSCompletionUtil.handleOverflow(resultSet)
      }

      if (!localProcessor.processingEncounteredAnyTypeAccess() && !localProcessor.isEncounteredDynamicClasses()) {
        val results = sink.getResultsAsObjects()
        val concat = when (expression) {
          is JSReferenceExpression -> results + conditionalCompilationVars(expression)
          else -> results
        }
        return JSCompletionUtil.pushVariants(concat, pushedSmartVariants, resultSet)
      }
    }
    else {
      val processor = ActionScriptQualifiedItemProcessor(sink)

      val originalQualifier = BaseJSSymbolProcessor.getOriginalQualifier(qualifier)
      JSResolveUtil.evaluateQualifierType(
        originalQualifier,
        originalQualifier.getContainingFile(),
        processor
      )
      if (sink.isOverflow) {
        JSCompletionUtil.handleOverflow(resultSet)
      }

      if (processor.noMoreResultsPossible()) {
        return JSCompletionUtil.pushVariants(sink.getResultsAsObjects(), pushedSmartVariants, resultSet)
      }

    }

    val resultsList: MutableList<Collection<LookupElement>> = mutableListOf()
    val processor = ActionScriptVariantsProcessor(
      containingFile,
      expression,
      parameters,
      resultSet
    )

    processor.addPushedVariants(pushedSmartVariants)

    val results = sink.getResultsAsObjects()
    if (!results.isEmpty()) {
      processor.populateCompletionList(results, resultSet)
      resultsList.add(results)
    }

    if (shouldProcessGlobalSymbols(expression)) {
      val stop = !ActionScriptSymbolCompletionUtil.processIndexedSymbols(expression, processor, resultSet, resultsList)
      if (stop) return resultsList.flatten()
    }

    if (qualifier == null && expression is JSReferenceExpression) {
      val conditionalCompilationVars = conditionalCompilationVars(expression)
      JSCompletionUtil.pushVariants(conditionalCompilationVars, pushedSmartVariants, resultSet)
      resultsList.add(conditionalCompilationVars)
    }

    resultsList.add(finishCompletion(resultSet, processor))

    return resultsList.flatten()
  }

  fun shouldProcessGlobalSymbols(element: PsiElement?): Boolean {
    return element != null
  }

  private fun finishCompletion(
    resultSet: CompletionResultSet,
    processor: ActionScriptVariantsProcessor
  ): Collection<LookupElement> {
    val results = processor.finalResults
    processor.populateCompletionList(results, resultSet)

    return results
  }


  private fun conditionalCompilationVars(expression: JSReferenceExpression): List<LookupElement> {
    val namespaceReference = JSReferenceExpressionImpl.getNamespaceReference(expression)
    return if (namespaceReference != null && namespaceReference.resolve() === namespaceReference)
      getConditionalCompilationConstantNamesForNamespace(expression, namespaceReference.getReferenceName())
    else
      getAllConditionalCompilationConstants(expression)
  }

  private fun getAllConditionalCompilationConstants(context: PsiElement): List<LookupElement> {
    val names: MutableList<LookupElement> = mutableListOf()
    val nameStrings: MutableSet<String> = mutableSetOf()
    val moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(context)
    for (provider in JSConditionalCompilationDefinitionsProvider.EP_NAME.extensionList) {
      for (conditional in provider.getAllConstants(moduleForPsiElement)) {
        if (!nameStrings.add(conditional)) continue
        val lookupElement: LookupElement = LookupElementBuilder.create(conditional, conditional)
        names.add(JSCompletionUtil.withJSLookupPriority(lookupElement, JSLookupPriority.CONDITIONAL_COMPILATION_CONSTANTS_PRIORITY))
      }
    }
    return names
  }

  private fun getConditionalCompilationConstantNamesForNamespace(context: PsiElement, namespace: String?): List<LookupElement> {
    val moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(context)
    return JSConditionalCompilationDefinitionsProvider.EP_NAME
      .extensionList
      .flatMap {
        it.getConstantNamesForNamespace(moduleForPsiElement, namespace)
      }
      .map { name -> LookupElementBuilder.create(name) }
      .toList()
  }
}
