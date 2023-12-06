// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2BlockParameterNameCompletionProvider : CompletionProvider<CompletionParameters>() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val block = parameters.position.parentOfType<Angular2HtmlBlock>() ?: return
    val definition = block.definition ?: return
    val uniqueParams = definition.parameters.filter { it.isUnique }.map { parameter -> parameter.name }.toSet()
    val providedParams = block.parameters.filter { uniqueParams.contains(it.name) }.mapNotNull { it.name }.toSet()

    for (param in definition.parameters) {
      if (!providedParams.contains(param.name) && param.pattern == null && !param.isPrimaryExpression) {
        WebSymbolCodeCompletionItem.create(param.name, 0, symbol = param)
          .withInsertHandlerAdded(BlockParameterNameInsertHandler)
          .addToResult(parameters, result)
      }
    }

    result.stopHere()
  }

  private object BlockParameterNameInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
      val document = context.document
      document.insertString(context.tailOffset, " ")
      context.editor.caretModel.moveToOffset(context.tailOffset)
    }
  }

}