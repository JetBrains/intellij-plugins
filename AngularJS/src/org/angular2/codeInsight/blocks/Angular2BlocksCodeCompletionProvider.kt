// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.applyIf
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.completion.WebSymbolCodeCompletionItem
import com.intellij.webSymbols.completion.WebSymbolsCompletionProviderBase
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import org.angular2.lang.html.psi.Angular2HtmlBlock

class Angular2BlocksCodeCompletionProvider : WebSymbolsCompletionProviderBase<Angular2HtmlBlock>() {
  override fun addCompletions(parameters: CompletionParameters,
                              result: CompletionResultSet,
                              position: Int,
                              name: String,
                              queryExecutor: WebSymbolsQueryExecutor,
                              context: Angular2HtmlBlock) {
    val blocksConfig = getAngular2HtmlBlocksConfig(context)
    val adjustedResult = result.withPrefixMatcher(name)


    val parentBlockDefinition = blocksConfig.definitions[(context.parent as? Angular2HtmlBlock)?.getName()]
      ?.takeIf { it.hasNestedSecondaryBlocks }

    val availableBlocks = if (parentBlockDefinition != null) {
      blocksConfig.secondaryBlocks[parentBlockDefinition.name] ?: emptyList()
    }
    else {
      val primaryBlockName =
        context.siblings(false, false)
          .filter { it.elementType != XmlTokenType.XML_WHITE_SPACE && it != XmlTokenType.XML_REAL_WHITE_SPACE }
          .takeWhile { it is Angular2HtmlBlock && blocksConfig.definitions[it.getName()]?.last != true }
          .firstNotNullOfOrNull { block -> (block as Angular2HtmlBlock).getName().takeIf { blocksConfig.primaryBlocksNames.contains(it) } }

      blocksConfig.primaryBlocks
        .plus(blocksConfig.secondaryBlocks[primaryBlockName] ?: emptyList())
    }

    availableBlocks.map { def ->
      WebSymbolCodeCompletionItem.create("@" + def.name, 0, symbol = def.symbol)
        .applyIf(!def.isPrimary) {
          withPriority(WebSymbol.Priority.HIGH)
        }
        .withInsertHandlerAdded(Angular2HtmlBlockInsertHandler)
    }
      .forEach {
        it.addToResult(parameters, adjustedResult)
      }

  }

  override fun getContext(position: PsiElement): Angular2HtmlBlock? =
    position.parent as? Angular2HtmlBlock
}