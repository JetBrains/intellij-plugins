package com.intellij.dts.util

import com.intellij.dts.lang.DtsTokenSets
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.siblings

object DtsUtil {
    /**
     * Splits the name of a node into node and unit address part. If the name
     * does not contain a unit address null will be returned. If the name
     * contains multiple @ the last one will be used as the separator.
     */
    fun splitName(name: String): Pair<String, String?> {
        return if (name.contains("@")) {
            val splitIndex = name.indexOfLast { it == '@' }

            val actualName = name.substring(0, splitIndex)
            val unitAddress = name.substring(splitIndex + 1)

            Pair(actualName, unitAddress)
        } else {
            Pair(name, null)
        }
    }

    /**
     * Iterates over the children of a psi element. If unfiltered is set to true
     * unproductive elements will be skipped (see isProductiveElement).
     */
    fun children(element: PsiElement, forward: Boolean = true, unfiltered: Boolean = false): Sequence<PsiElement> {
        val start = if (forward) element.firstChild else element.lastChild

        val siblings = start.siblings(forward = forward)
        if (unfiltered) return siblings

        return siblings.filter {
            val type = it.elementType
            type != null && isProductiveElement(type)
        }
    }

    /**
     * A token is considered productive if it is none of the following:
     * - not null
     * - comment
     * - whit space
     * - any kind of preprocessor statement
     */
    fun isProductiveElement(type: IElementType?): Boolean {
        if (type == null) return false

        return type != TokenType.WHITE_SPACE && type !in DtsTokenSets.comments && type !in DtsTokenSets.preprocessorStatements
    }
}