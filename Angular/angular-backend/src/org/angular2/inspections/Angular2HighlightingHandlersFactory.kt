package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSHighlightingHandlersFactory
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.highlighting.JSKeywordHighlighterVisitor
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lang.javascript.highlighting.TypeScriptKeywordHighlighterVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import org.angular2.codeInsight.blocks.BLOCK_FOR
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.expr.psi.Angular2DeferredTimeLiteralExpression

class Angular2HighlightingHandlersFactory: JSHighlightingHandlersFactory() {
  override fun createKeywordHighlighterVisitor(holder: HighlightInfoHolder, dialectOptionHolder: DialectOptionHolder): JSKeywordHighlighterVisitor {
    return object : TypeScriptKeywordHighlighterVisitor(holder) {
      override fun visitElement(element: PsiElement) {
        when (element) {
          is Angular2BlockParameter -> if (element.block?.getName() == BLOCK_FOR && element.isPrimaryExpression)
            element.node.findChildByType(JSTokenTypes.IDENTIFIER)
              ?.let { highlightKeyword(it, TypeScriptHighlighter.TS_KEYWORD) }
              ?.let { myHolder.add(it) }
          is Angular2DeferredTimeLiteralExpression -> element.childLeafs()
            .find { it.elementType == JSTokenTypes.IDENTIFIER }
            ?.takeIf { it.text == "s" || it.text == "ms" }
            ?.let { highlightKeyword(it.node, TypeScriptHighlighter.TS_NUMBER) }
            ?.let { myHolder.add(it) }
          else -> super.visitElement(element)
        }
      }
    }

  }

  override fun getInspectionSuppressor(): InspectionSuppressor {
    return Angular2InspectionSuppressor
  }

}