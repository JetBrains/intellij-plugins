// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.psi.impl

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSExpressionImpl
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType
import com.intellij.lang.javascript.psi.types.evaluable.JSQualifiedReferenceType
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.tree.IElementType
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2EntitiesProvider.findPipes
import org.angular2.lang.expr.parser.Angular2ElementTypes
import org.angular2.lang.expr.psi.Angular2ElementVisitor
import org.angular2.lang.expr.psi.Angular2PipeExpression
import org.angular2.lang.expr.psi.Angular2PipeLeftSideArgument

class Angular2PipeExpressionImpl(elementType: IElementType?)
  : JSExpressionImpl(elementType), Angular2PipeExpression, JSCallLikeExpressionCommon, JSEvaluableElement {
  override fun accept(visitor: PsiElementVisitor) {
    when (visitor) {
      is Angular2ElementVisitor -> visitor.visitAngular2PipeExpression(this)
      is JSElementVisitor -> visitor.visitJSCallExpression(this)
      else -> super.accept(visitor)
    }
  }

  override fun getIndexingData(): JSElementIndexingData? {
    return null
  }

  override fun getName(): String? {
    return nameReference?.referenceName
  }

  override fun getMethodExpression(): JSExpression? {
    return nameReference
  }

  override fun getStubSafeMethodExpression(): JSExpression? {
    return null
  }

  override fun getArgumentList(): JSArgumentList {
    return leftSideArgument
  }

  private val leftSideArgument: Angular2PipeLeftSideArgument
    get() {
      val node = findChildByType(Angular2ElementTypes.PIPE_LEFT_SIDE_ARGUMENT)!!
      return node.getPsi(Angular2PipeLeftSideArgument::class.java)
    }

  override fun isRequireCall(): Boolean {
    return false
  }

  override fun isDefineCall(): Boolean {
    return false
  }

  override fun isElvis(): Boolean {
    return false
  }

  private val nameReference: JSReferenceExpression?
    get() = findPsiChildByType(Angular2ElementTypes.PIPE_REFERENCE_EXPRESSION) as JSReferenceExpression?

  override fun evaluate(evaluateContext: JSEvaluateContext, typeProcessor: JSTypeProcessor): Boolean {
    val methodExpression = methodExpression
    val name = name
    if (methodExpression == null || name == null) return true
    val scope = Angular2DeclarationsScope(methodExpression)
    val pipe = findPipes(project, name).find { scope.contains(it) }
               ?: return true
    val jsClass = pipe.typeScriptClass ?: return true
    val typeSource = JSTypeSourceFactory.createTypeSource(this, true)
    val instanceMethod = JSQualifiedReferenceType(Angular2EntitiesProvider.TRANSFORM_METHOD, jsClass.jsType, typeSource)
    val factory = JSPsiBasedTypeOfType.getArgumentTypeFactory(evaluateContext.isContextualOverloadEvaluation)
    val type = JSApplyCallType(instanceMethod, getArgumentTypes(factory), typeSource)
    typeProcessor.process(type, evaluateContext)
    return true
  }
}