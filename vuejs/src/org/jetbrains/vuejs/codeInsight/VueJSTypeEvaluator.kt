// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.resolve.*
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeContext
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.lang.javascript.psi.types.primitives.JSSymbolType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForVariable

class VueJSTypeEvaluator(context: JSEvaluateContext, processor: JSTypeProcessor, helper: JSTypeEvaluationHelper)
  : JSTypeEvaluator(context, processor, helper) {

  override fun addTypeFromVariableResolveResult(jsVariable: JSFieldVariable) {
    if (evaluateTypeFromVForVariable(jsVariable)) return
    super.addTypeFromVariableResolveResult(jsVariable)
  }

  private fun evaluateTypeFromVForVariable(jsVariable: JSFieldVariable): Boolean {
    if (jsVariable !is VueJSVForVariable) return false
    val vForExpression = PsiTreeUtil.getParentOfType(jsVariable, VueJSVForExpression::class.java) ?: return false

    val collectionExpr = vForExpression.getCollectionExpression() ?: return false

    val declaration = PsiTreeUtil.getTopmostParentOfType(jsVariable, JSInitializerOwner::class.java)
                      ?: jsVariable

    when (vForExpression.getVarStatement()?.declarations?.indexOf(declaration)) {
      0 -> {
        pushDestructuringContext(jsVariable)
        val expression = myContext.processedExpression
        when (val collectionType = JSResolveUtil.getElementJSType(collectionExpr)) {
          is JSStringType -> addType(collectionType, expression)
          is JSNumberType -> addType(collectionType, expression)
          else -> getComponentTypeFromArrayExpression(expression, collectionExpr)
            .forEach { addType(it, expression) }
        }
        restoreEvaluationContextApplingElementsSize(myContext.jsElementsToApply.size)
        myContext.finishEvaluationWithStrictness(myContext.isStrict)
      }
      1 -> {
        val collectionType = JSResolveUtil.getElementJSType(collectionExpr)?.substitute()
        if (collectionType == null || JSTypeUtils.isAnyType(collectionType)) {
          addVForVarType(collectionExpr, ::JSNumberType, ::JSStringType, ::JSSymbolType)
        }
        else if (JSTypeUtils.isArrayLikeType(collectionType) || collectionType is JSPrimitiveType) {
          addVForVarType(collectionExpr, ::JSNumberType)
        }
        else {
          val recordType = collectionType.asRecordType()
          when {
            recordType.findIndexer(JSRecordType.IndexSignatureKind.NUMERIC) != null ->
              addVForVarType(collectionExpr, ::JSNumberType)

            recordType.findIndexer(JSRecordType.IndexSignatureKind.STRING) != null ->
              addVForVarType(collectionExpr, ::JSStringType)

            else -> addVForVarType(collectionExpr, ::JSNumberType, ::JSStringType, ::JSSymbolType)
          }
        }
      }
      2 -> addVForVarType(collectionExpr, ::JSNumberType)
    }
    return true
  }

  private fun addVForVarType(source: PsiElement, vararg types: (Boolean, JSTypeSource, JSTypeContext) -> JSType) {
    val typeSource = JSTypeSourceFactory.createTypeSource(source, false)
    addType(JSCompositeTypeImpl.getCommonType(types.map { it(true, typeSource, JSTypeContext.INSTANCE) }, typeSource, false), source, true)
  }

  companion object {
    fun resolveEventType(@Suppress("UNUSED_PARAMETER") attribute: XmlAttribute): JSType? {
      // TODO resolve event type
      return null
    }
  }
}
