package org.jetbrains.vuejs.language

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSParenthesizedExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.util.PsiTreeUtil

object VueElementTypes {
  val FILE: IFileElementType = JSFileElementType.create(VueJSLanguage.INSTANCE)
  val EMBEDDED_JS: JSEmbeddedContentElementType = object : JSEmbeddedContentElementType(VueJSLanguage.INSTANCE, "VueJS") {
    override fun createStripperLexer(baseLanguage: Language): Lexer? = null
  }
  val V_FOR_EXPRESSION: VueJSCompositeElementType = object : VueJSCompositeElementType("V_FOR_EXPRESSION") {
    override fun createCompositeNode(): ASTNode = VueVForExpression(this)
  }
}

abstract class VueJSCompositeElementType(debugName: String) : IElementType(debugName, VueJSLanguage.INSTANCE), ICompositeElementType

class VueVForExpression(vueJSElementType: IElementType) : JSExpressionImpl(vueJSElementType) {
  fun getVarStatement(): JSVarStatement? {
    if (firstChild is JSVarStatement) return firstChild as JSVarStatement
    if (firstChild is JSParenthesizedExpression) {
      return PsiTreeUtil.findChildOfType(firstChild, JSVarStatement::class.java)
    }
    return null
  }
  fun getReferenceExpression(): PsiElement? = children.firstOrNull { it is JSReferenceExpression }
}
