// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl
import com.intellij.lang.javascript.psi.impl.JSForInStatementImpl
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
import org.jetbrains.annotations.ApiStatus

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

// This class is the original `VueVForExpression` class,
// but it's renamed to allow instanceof check through deprecated class from 'language' package
@Deprecated("Public for internal purpose only!")
@ApiStatus.ScheduledForRemoval(inVersion = "2019.3")
open class _VueVForExpression(vueJSElementType: IElementType) : JSExpressionImpl(vueJSElementType) {
  fun getVarStatement(): JSVarStatement? {
    if (firstChild is JSVarStatement) return firstChild as JSVarStatement
    if (firstChild is JSParenthesizedExpression) {
      return PsiTreeUtil.findChildOfType(firstChild, JSVarStatement::class.java)
    }
    return null
  }

  fun getReferenceExpression(): PsiElement? = children.firstOrNull { it is JSReferenceExpression }

  fun getCollectionExpression(): JSExpression? {
    return JSForInStatementImpl.findCollectionExpression(this)
  }
}

class VueVForExpression(vueJSElementType: IElementType) : org.jetbrains.vuejs.language.VueVForExpression(vueJSElementType)

class VueVForVariable(node: ASTNode?) : JSVariableImpl<JSVariableStubBase<JSVariable>, JSVariable>(node) {
  override fun hasBlockScope(): Boolean = true

  override fun getDeclarationScope(): PsiElement? =
    PsiTreeUtil.getContextOfType(this, XmlTag::class.java, PsiFile::class.java)
}
