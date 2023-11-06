package com.intellij.dts.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeaf

private val prefixTokens = TokenSet.create(
    DtsTypes.NAME,
    DtsTypes.SLASH,
    *DtsTokenSets.compilerDirectives.types,
)

private fun collectPrefixTokens(parameters: CompletionParameters): Sequence<PsiElement> = sequence {
    var position: PsiElement? = parameters.position

    while (position != null && position.elementType in prefixTokens) {
        yield(position)
        position = position.prevLeaf(true)
    }
}

fun CompletionResultSet.withDtsPrefixMatcher(parameters: CompletionParameters): CompletionResultSet {
    var prefix = collectPrefixTokens(parameters).fold("") { acc, token -> "${token.text}$acc"}

    // remove dummy identifier from prefix
    prefix = prefix.removeSuffix(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)

    return withPrefixMatcher(prefix)
}