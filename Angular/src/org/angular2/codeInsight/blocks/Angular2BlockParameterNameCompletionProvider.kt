// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import com.intellij.util.ProcessingContext
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2BlockParameterNameCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val block = parameters.position.parentOfType<Angular2HtmlBlock>() ?: return
    val definition = block.definition ?: return
    val uniqueParams = definition.parameters.filter { it.isUnique }.map { parameter -> parameter.name }.toSet()
    val providedParams = block
      .parameters
      .filter { uniqueParams.contains(it.name) }
      .mapNotNull { it.name }
      .plus(
        parameters.position.siblings(false, false)
          .filter { it.elementType == Angular2TokenTypes.BLOCK_PARAMETER_NAME }
          .map { it.text }
      )
      .toSet()

    for (param in definition.parameters) {
      if (!providedParams.contains(param.name) && param.pattern == null && !param.isPrimaryExpression) {
        WebSymbolCodeCompletionItem.create(param.name, 0, symbol = param)
          .withInsertHandlerAdded(Angular2BlockKeywordInsertHandler)
          .addToResult(parameters, result)
      }
    }

    result.stopHere()
  }

}