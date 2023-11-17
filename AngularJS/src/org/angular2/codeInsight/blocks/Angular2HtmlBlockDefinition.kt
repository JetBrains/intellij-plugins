// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.query.WebSymbolsQueryExecutor
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import com.intellij.webSymbols.utils.qualifiedKind
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.NG_BLOCKS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.NG_BLOCK_PARAMETERS

fun getAngular2HtmlBlocksConfig(location: PsiElement): Angular2HtmlBlocksConfig {
  val file = location.containingFile.originalFile
  return CachedValuesManager.getCachedValue(file) {
    val queryExecutor = WebSymbolsQueryExecutorFactory.create(file, false)
    CachedValueProvider.Result.create(Angular2HtmlBlocksConfig(queryExecutor.runListSymbolsQuery(NG_BLOCKS, true)
                                                                 .map { Angular2HtmlBlockDefinition(it, queryExecutor) }
                                                                 .associateBy { it.name }), queryExecutor)
  }
}

class Angular2HtmlBlocksConfig(private val definitions: Map<String, Angular2HtmlBlockDefinition>) {

  val primaryBlocks: List<Angular2HtmlBlockDefinition> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    definitions.values.filter { it.isPrimary }
  }

  val secondaryBlocks: Map<String, List<Angular2HtmlBlockDefinition>> by lazy(LazyThreadSafetyMode.PUBLICATION) {
    definitions.values.filter { !it.isPrimary && it.primaryBlock != null }.groupBy { it.primaryBlock!! }
  }

  operator fun get(block: Angular2HtmlBlock?): Angular2HtmlBlockDefinition? =
    definitions[block?.getName()]

  operator fun get(blockName: String?): Angular2HtmlBlockDefinition? =
    definitions[blockName]

}

class Angular2HtmlBlockDefinition(val symbol: WebSymbol, private val queryExecutor: WebSymbolsQueryExecutor) {

  init {
    assert(symbol.qualifiedKind == NG_BLOCKS)
  }

  val name: String
    get() = symbol.name

  val isPrimary: Boolean
    get() = symbol.properties["is-primary"] == true

  val primaryBlock: String?
    get() = symbol.properties["primary-block"] as? String

  val maxCount: Int?
    get() = (symbol.properties["max-count"] as? Number)?.toInt()

  val last: Boolean
    get() = symbol.properties["order"] == "last"

  val preferredLast: Boolean
    get() = symbol.properties["order"] == "preferred-last"

  val hasNestedSecondaryBlocks: Boolean
    get() = symbol.properties["nested-secondary-blocks"] == true

  val parameters: List<Angular2HtmlBlockParameterDefinition>
    get() = queryExecutor.runListSymbolsQuery(NG_BLOCK_PARAMETERS, true, scope = listOf(symbol))
      .map { Angular2HtmlBlockParameterDefinition(it) }

  override fun equals(other: Any?): Boolean =
    other === this
    || other is Angular2HtmlBlockDefinition && other.symbol == symbol && other.queryExecutor == queryExecutor

  override fun hashCode(): Int =
    symbol.hashCode()

}

data class Angular2HtmlBlockParameterDefinition(val symbol: WebSymbol) {

  init {
    assert(symbol.qualifiedKind == NG_BLOCK_PARAMETERS)
  }

  val name: String
    get() = symbol.name

  val maxCount: Int?
    get() = (symbol.properties["max-count"] as? Number)?.toInt()

  val position: Int?
    get() = (symbol.properties["position"] as? Number)?.toInt()

  val required: Boolean
    get() = symbol.properties["required"] == true

  val nameAsPrefix: Boolean
    get() = symbol.properties["name-as-prefix"] != false

}
