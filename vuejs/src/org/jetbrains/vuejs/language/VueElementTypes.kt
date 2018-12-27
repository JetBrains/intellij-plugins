package org.jetbrains.vuejs.language

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.JSParenthesizedExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.stubs.JSVariableStubBase
import com.intellij.lang.javascript.types.JSEmbeddedContentElementType
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag

object VueElementTypes {
  val EMBEDDED_JS: JSEmbeddedContentElementType = object : JSEmbeddedContentElementType(VueJSLanguage.INSTANCE, "VueJS") {
    override fun createStripperLexer(baseLanguage: Language): Lexer? = null
  }

  val V_FOR_EXPRESSION: VueJSCompositeElementType = object : VueJSCompositeElementType("V_FOR_EXPRESSION") {
    override fun createCompositeNode(): ASTNode = VueVForExpression(this)
  }

  val V_FOR_VARIABLE: JSVariableElementType = object : JSVariableElementType("V_FOR_VARIABLE") {
    override fun construct(node: ASTNode?): PsiElement? {
      return VueVForVariable(node)
    }

    override fun shouldCreateStub(node: ASTNode?): Boolean {
      return false
    }
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

class VueVForVariable(node: ASTNode?) : JSVariableImpl<JSVariableStubBase<JSVariable>, JSVariable>(node) {
  override fun hasBlockScope(): Boolean = true

  override fun getDeclarationScope(): PsiElement? =
    PsiTreeUtil.getContextOfType(this, XmlTag::class.java, PsiFile::class.java)
}