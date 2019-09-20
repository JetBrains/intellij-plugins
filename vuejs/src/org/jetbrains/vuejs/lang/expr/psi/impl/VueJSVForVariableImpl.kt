// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.stubs.JSVariableStubBase
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeContext
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForVariable

class VueJSVForVariableImpl(node: ASTNode?) : JSVariableImpl<JSVariableStubBase<JSVariable>, JSVariable>(node), VueJSVForVariable {
  override fun hasBlockScope(): Boolean = true

  override fun calculateType(): JSType? {
    val vForExpression = PsiTreeUtil.getParentOfType(this, VueJSVForExpression::class.java) ?: return null
    val vars = vForExpression.getVarStatement()?.variables ?: return null

    val collectionExpr = vForExpression.getCollectionExpression()

    when (vars.indexOf(this)) {
      0 -> return JSResolveUtil.getElementJSType(this)
      1 -> {
        val collectionType = JSResolveUtil.getElementJSType(collectionExpr)?.substitute()
        val source = JSTypeSourceFactory.createTypeSource(collectionExpr, false)
        if (collectionType == null || JSTypeUtils.isAnyType(collectionType)) {
          return JSCompositeTypeImpl.getCommonType(
            JSNumberType(true, source, JSTypeContext.INSTANCE),
            JSStringType(true, source, JSTypeContext.INSTANCE),
            source,
            false
          )
        }
        else if (JSTypeUtils.isArrayLikeType(collectionType)
                 || JSTypeUtils.isIterableCollectionType(collectionType)
                 || collectionType is JSPrimitiveType) {
          return JSNumberType(true, source, JSTypeContext.INSTANCE)
        }
        else {
          return JSTypeUtils.getIndexableComponentType(collectionType)
                 ?: JSStringType(true, source, JSTypeContext.INSTANCE)
        }
      }
      2 -> return JSNumberType(true, JSTypeSourceFactory.createTypeSource(collectionExpr, false), JSTypeContext.INSTANCE)
    }
    return null
  }

  override fun getDeclarationScope(): PsiElement? =
    PsiTreeUtil.getContextOfType(this, XmlTag::class.java, PsiFile::class.java)
}
