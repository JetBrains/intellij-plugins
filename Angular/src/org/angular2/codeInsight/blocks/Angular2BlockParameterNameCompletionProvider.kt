// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import com.intellij.util.ProcessingContext
import com.intellij.util.applyIf
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2BlockParameterNameCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val parameter = parameters.position.parentOfType<Angular2BlockParameter>() ?: return
    val block = parameter.parentOfType<Angular2HtmlBlock>() ?: return
    val definition = block.definition ?: return
    val uniqueParams = definition.parameters.filter { it.isUnique }.map { it.name }.toSet()
    val providedParams = block
      .parameters
      .filter { uniqueParams.contains(it.name) }
      .mapNotNull { it.name }
      .plus(
        parameters.position.siblings(false, false)
          .filter { it.elementType == Angular2TokenTypes.BLOCK_PARAMETER_NAME }
          .map { it.text }
      )
      .toMutableSet()

    val candidates = mutableListOf<WebSymbol>()

    if (parameters.position.siblings(false, false)
        .none {
          it.elementType == Angular2TokenTypes.BLOCK_PARAMETER_NAME
          || it.elementType == Angular2TokenTypes.BLOCK_PARAMETER_PREFIX
        })
    // Completion on first name token, so it can be a prefix
      candidates.addAll(definition.parameterPrefixes)
    else
    // Completion on second name token, so it cannot be a prefix.
    // Add parameters specific to the prefix if any
      parameter.prefixDefinition?.parameters?.let { candidates.addAll(it) }

    // Parameters from prefix definition should have priority,
    // so add regular ones at the end of the list
    candidates.addAll(definition.parameters)

    for (param in candidates) {
      if (param.pattern == null && (param !is Angular2BlockParameterSymbol || !param.isPrimaryExpression) && providedParams.add(param.name)) {
        WebSymbolCodeCompletionItem.create(param.name, 0, symbol = param)
          .applyIf(param !is Angular2BlockParameterSymbol || param.hasContent) {
            withInsertHandlerAdded(Angular2BlockKeywordInsertHandler)
          }
          .addToResult(parameters, result)
      }
    }

    result.stopHere()
  }

}