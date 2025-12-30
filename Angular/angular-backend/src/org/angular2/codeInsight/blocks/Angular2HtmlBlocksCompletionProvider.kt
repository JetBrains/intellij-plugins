// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.HtmlCompletionContributor
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.completion.PolySymbolsCompletionProviderBase
import com.intellij.polySymbols.query.PolySymbolQueryExecutor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlTokenType
import com.intellij.util.applyIf
import com.intellij.util.asSafely
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlBlockContents

class Angular2HtmlBlocksCompletionProvider : PolySymbolsCompletionProviderBase<PsiElement>() {
  override fun addCompletions(
    parameters: CompletionParameters,
    result: CompletionResultSet,
    position: Int,
    name: String,
    queryExecutor: PolySymbolQueryExecutor,
    context: PsiElement,
  ) {
    val blocksConfig = getAngular2HtmlBlocksConfig(context)
    val adjustedResult = result.withPrefixMatcher(name).applyIf(context !is Angular2HtmlBlock) {
      HtmlCompletionContributor.patchResultSetForHtmlElementInTextCompletion(this, parameters)
    }

    val parentPrimaryBlockName = blocksConfig[context.parent.asSafely<Angular2HtmlBlockContents>()?.block]
      ?.takeIf { it.hasNestedSecondaryBlocks }
      ?.name

    val prevBlocksCount = context.previousBlocks()
      .takeWhile { blocksConfig[it]?.isPrimary != true }
      .groupBy { it.getName() }
      .mapValues { it.value.count() }

    val availableBlocks = if (parentPrimaryBlockName != null) {
      blocksConfig.secondaryBlocks[parentPrimaryBlockName] ?: emptyList()
    }
    else {
      val primaryBlockName =
        context.previousBlocks()
          .firstOrNull()
          ?.let { blocksConfig[it] }
          ?.takeIf { !it.last }
          ?.let {
            if (it.isPrimary) it.name
            else it.primaryBlock
          }
          ?.takeIf { blocksConfig[it]?.hasNestedSecondaryBlocks != true }

      blocksConfig.primaryBlocks
        .plus(blocksConfig.secondaryBlocks[primaryBlockName] ?: emptyList())

    }

    availableBlocks
      .filter { def -> !def.isUnique || (prevBlocksCount[def.name] ?: 0) == 0 }
      .map { def ->
        PolySymbolCodeCompletionItem.create("@" + def.name, 0, symbol = def) {
          priority(if (!def.isPrimary) PolySymbol.Priority.HIGH else PolySymbol.Priority.NORMAL)
          insertHandler(Angular2HtmlBlockInsertHandler)
        }
      }
      .forEach {
        it.addToResult(parameters, adjustedResult)
      }

  }

  private fun PsiElement.previousBlocks(): Sequence<Angular2HtmlBlock> =
    siblings(false, false)
      .filter { element ->
        element.elementType != XmlTokenType.XML_WHITE_SPACE && element != XmlTokenType.XML_REAL_WHITE_SPACE
        && (element !is XmlText || !element.text.all { it.isWhitespace() })
      }
      .takeWhile { it is Angular2HtmlBlock }
      .filterIsInstance<Angular2HtmlBlock>()

  override fun getContext(position: PsiElement): PsiElement? =
    when (position.elementType) {
      Angular2HtmlTokenTypes.BLOCK_NAME -> position.parent as? Angular2HtmlBlock
      XmlTokenType.XML_DATA_CHARACTERS -> position.parent.takeIf { it !is XmlDocument } ?: position
      else -> null
    }
}