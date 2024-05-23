package com.intellij.dts.pp.lang.psi

import com.intellij.dts.pp.lang.parser.PpAdHocParser
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.tree.util.children
import com.intellij.openapi.util.TextRange
import kotlin.math.max
import kotlin.math.min

open class PpStatementPsiElement(node: ASTNode, private val parser: PpAdHocParser) : ASTWrapperPsiElement(node) {
  private fun TextRange.relativeTo(parent: TextRange): TextRange {
    val start = max(0, startOffset - parent.startOffset)
    val end = max(0, endOffset - parent.startOffset)

    return TextRange(
      min(parent.length, start),
      min(parent.length, end),
    )
  }

  val statement: PpStatement
    get() {
      val tokens = node.children()
        .dropWhile { child -> child.elementType != parser.tokenTypes.directive }
        .map { child -> PpToken(child.elementType, child.text, child.textRange.relativeTo(textRange)) }
        .toList()

      return parser.parse(tokens)
    }
}
