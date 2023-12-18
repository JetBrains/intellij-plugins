package com.intellij.dts.completion.provider

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeaf

private fun collectPrefixTokens(parameters: CompletionParameters, tokens: TokenSet): Sequence<PsiElement> = sequence {
  var position: PsiElement? = parameters.position

  while (position != null && position.elementType in tokens) {
    yield(position)
    position = position.prevLeaf(true)
  }
}

private fun collectPrefix(parameters: CompletionParameters, tokens: TokenSet): String {
  val prefix = collectPrefixTokens(parameters, tokens).fold("") { acc, token -> "${token.text}$acc" }

  // remove dummy identifier from prefix
  return prefix.removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)
}

private val prefixTokens = TokenSet.create(
  DtsTypes.NAME,
  DtsTypes.SLASH,
  *DtsTokenSets.compilerDirectives.types,
)

fun CompletionResultSet.withDtsPrefixMatcher(parameters: CompletionParameters): CompletionResultSet =
  withPrefixMatcher(collectPrefix(parameters, prefixTokens))

private val intPrefixTokens = TokenSet.create(
  DtsTypes.INT_LITERAL,
  DtsTypes.NAME,
)

fun CompletionResultSet.withDtsIntPrefixMatcher(parameters: CompletionParameters): CompletionResultSet =
  withPrefixMatcher(collectPrefix(parameters, intPrefixTokens))
