// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.lang.javascript.JSKeywordSets
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.*
import com.intellij.util.asSafely
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.query.WebSymbolsQueryExecutorFactory
import org.angular2.codeInsight.template.getTemplateElementsScopeFor
import org.angular2.lang.expr.lexer.Angular2TokenTypes
import org.angular2.lang.expr.psi.Angular2Action
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression
import org.angular2.lang.expr.psi.impl.Angular2BlockParameterVariableImpl
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.web.NG_BLOCKS

const val BLOCK_IF: String = "if"
const val BLOCK_ELSE_IF: String = "else if"
const val BLOCK_ELSE: String = "else"
const val BLOCK_SWITCH: String = "switch"
const val BLOCK_CASE: String = "case"
const val BLOCK_DEFAULT: String = "default"
const val BLOCK_FOR: String = "for"
const val BLOCK_EMPTY: String = "empty"
const val BLOCK_DEFER: String = "defer"
const val BLOCK_ERROR: String = "error"
const val BLOCK_PLACEHOLDER: String = "placeholder"
const val BLOCK_LOADING: String = "loading"
const val BLOCK_LET: String = "let"

val BLOCKS_WITH_PRIMARY_EXPRESSION: Set<String> = setOf(BLOCK_IF, BLOCK_ELSE_IF, BLOCK_SWITCH, BLOCK_CASE, BLOCK_FOR, BLOCK_LET)

const val PARAMETER_AS: String = "as"
const val PARAMETER_LET: String = "let"
const val PARAMETER_ON: String = "on"
const val PARAMETER_WHEN: String = "when"
const val PARAMETER_NEVER: String = "never"
const val PARAMETER_TRACK: String = "track"

const val PARAMETER_PREFIX_PREFETCH: String = "prefetch"
const val PARAMETER_PREFIX_HYDRATE: String = "hydrate"

object Angular2HtmlBlockUtils {

  private val WHITESPACES = Regex("[ \t]+")

  fun String.toCanonicalBlockName(): String =
    removePrefix("@").replace(WHITESPACES, " ")

}

fun isJSReferenceInForBlockLetParameterAssignment(ref: JSReferenceExpression): Boolean =
  ref.parent is Angular2BlockParameterVariableImpl
  && ref.parent.parent?.parent.let { it is Angular2BlockParameter && it.name == PARAMETER_LET && it.block?.getName() == BLOCK_FOR }

fun isJSReferenceAfterEqInForBlockLetParameterAssignment(ref: JSReferenceExpression): Boolean =
  isJSReferenceInForBlockLetParameterAssignment(ref)
  && ref
    .siblings(false, false)
    .filter { it.elementType != TokenType.WHITE_SPACE }
    .firstOrNull()?.elementType == JSTokenTypes.EQ

fun isDeferOnTriggerReference(ref: JSReferenceExpression): Boolean =
  ref.siblings(false, false)
    .filter { it.elementType != TokenType.WHITE_SPACE }
    .firstOrNull()
    ?.let { it.elementType == Angular2TokenTypes.BLOCK_PARAMETER_NAME && it.textMatches(PARAMETER_ON) } == true

fun isDeferOnTriggerParameterReference(ref: JSReferenceExpression): Boolean =
  ref.parent.asSafely<Angular2BlockParameter>()?.name == PARAMETER_ON
  && ref.siblings(false, false)
    .filter { it.elementType != TokenType.WHITE_SPACE }
    .firstOrNull()
    ?.elementType != Angular2TokenTypes.BLOCK_PARAMETER_NAME

fun getAngular2HtmlBlocksConfig(location: PsiElement): Angular2HtmlBlocksConfig {
  val file = location.containingFile.originalFile
  return CachedValuesManager.getCachedValue(file) {
    val queryExecutor = WebSymbolsQueryExecutorFactory.create(file, false)
    CachedValueProvider.Result.create(Angular2HtmlBlocksConfig(
      queryExecutor
        .runListSymbolsQuery(NG_BLOCKS, true)
        .filterIsInstance<Angular2HtmlBlockSymbol>()
        .associateBy { it.name }), queryExecutor)
  }
}

fun isDeferOnReferenceExpression(element: PsiElement): Boolean =
/* Identifier within JSReferenceExpression or JSReferenceExpression itself */
  (element.asSafely<JSReferenceExpression>()
   ?: element.takeIf { JSKeywordSets.IDENTIFIER_NAMES.contains(it.elementType) }
     ?.parent
     ?.asSafely<JSReferenceExpression>()
  )
    ?.parent
    ?.asSafely<Angular2BlockParameter>()
    ?.name == PARAMETER_ON

fun getDeferOnTriggerDefinition(parameter: Angular2BlockParameter): WebSymbol? {
  val triggerName = parameter
    .takeIf { it.name == PARAMETER_ON }
    ?.childrenOfType<JSReferenceExpression>()
    ?.firstOrNull()
    ?.referenceName
  return parameter.definition?.triggers?.find { it.name == triggerName }
}

fun isLetDeclarationVariable(node: PsiElement): Boolean =
  node is Angular2BlockParameterVariableImpl
  && node.parentOfType<Angular2HtmlBlock>()?.definition?.name == BLOCK_LET

fun isLetReferenceBeforeDeclaration(ref: JSReferenceExpression, declaration: PsiElement): Boolean =
  isLetDeclarationVariable(declaration)
  && declaration is Angular2BlockParameterVariableImpl
  && declaration.endOffset >= ref.startOffset
  && ref.parentOfType<Angular2EmbeddedExpression>() !is Angular2Action
  && getTemplateElementsScopeFor(ref) == getTemplateElementsScopeFor(declaration)

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