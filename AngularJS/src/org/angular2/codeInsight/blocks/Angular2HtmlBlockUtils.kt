// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.web.Angular2WebSymbolsQueryConfigurator

const val BLOCK_IF = "if"
const val BLOCK_ELSE_IF = "else if"
const val BLOCK_ELSE = "else"
const val BLOCK_SWITCH = "switch"
const val BLOCK_CASE = "case"
const val BLOCK_DEFAULT = "default"

object Angular2HtmlBlockUtils {

  private val WHITESPACES = Regex("[ \t]+")

  fun String.toCanonicalBlockName() =
    removePrefix("@").replace(WHITESPACES, " ")

}


fun getAngular2HtmlBlocksConfig(location: PsiElement): Angular2HtmlBlocksConfig {
  val file = location.containingFile.originalFile
  return CachedValuesManager.getCachedValue(file) {
    val queryExecutor = WebSymbolsQueryExecutorFactory.create(file, false)
    CachedValueProvider.Result.create(Angular2HtmlBlocksConfig(
      queryExecutor
        .runListSymbolsQuery(Angular2WebSymbolsQueryConfigurator.NG_BLOCKS, true)
        .filterIsInstance<Angular2HtmlBlockSymbol>()
        .associateBy { it.name }), queryExecutor)
  }
}

class Angular2HtmlBlocksConfig(private val definitions: Map<String, Angular2HtmlBlockSymbol>) {

  val primaryBlocks: List<Angular2HtmlBlockSymbol> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    definitions.values.filter { it.isPrimary }
  }

  val secondaryBlocks: Map<String, List<Angular2HtmlBlockSymbol>> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    definitions.values.filter { !it.isPrimary && it.primaryBlock != null }.groupBy { it.primaryBlock!! }
  }

  operator fun get(block: Angular2HtmlBlock?): Angular2HtmlBlockSymbol? =
    definitions[block?.getName()]

  operator fun get(blockName: String?): Angular2HtmlBlockSymbol? =
    definitions[blockName]

}