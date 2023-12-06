// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.impl.Angular2BlockParameterVariableImpl
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.web.Angular2WebSymbolsQueryConfigurator

const val BLOCK_IF = "if"
const val BLOCK_ELSE_IF = "else if"
const val BLOCK_ELSE = "else"
const val BLOCK_SWITCH = "switch"
const val BLOCK_CASE = "case"
const val BLOCK_DEFAULT = "default"
const val BLOCK_FOR = "for"
const val BLOCK_DEFER = "defer"
const val BLOCK_PLACEHOLDER = "placeholder"
const val BLOCK_LOADING = "loading"

val BLOCKS_WITH_PRIMARY_EXPRESSION = setOf(BLOCK_IF, BLOCK_ELSE_IF, BLOCK_SWITCH, BLOCK_CASE, BLOCK_FOR)

const val PARAMETER_AS = "as"
const val PARAMETER_LET = "let"

object Angular2HtmlBlockUtils {

  private val WHITESPACES = Regex("[ \t]+")

  fun String.toCanonicalBlockName() =
    removePrefix("@").replace(WHITESPACES, " ")

}

fun isJSReferenceInForBlockLetParameterAssignment(ref: JSReferenceExpression) =
  ref.parent is Angular2BlockParameterVariableImpl
  && ref.parent.parent?.parent.let { it is Angular2BlockParameter && it.name == PARAMETER_LET && it.block?.getName() == BLOCK_FOR }

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