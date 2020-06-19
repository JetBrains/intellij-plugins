// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluationHelper
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForVariable

class VueJSTypeEvaluator(context: JSEvaluateContext, processor: JSTypeProcessor, helper: JSTypeEvaluationHelper)
  : TypeScriptTypeEvaluator(context, processor, helper) {

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
        when (val collectionType = JSResolveUtil.getElementJSType(collectionExpr)?.substitute()) {
          is JSStringType -> addVForVarType(collectionExpr, ::JSStringType)
          is JSNumberType -> addVForVarType(collectionExpr, ::JSNumberType)
          is JSType -> {
            val type = JSTypeUtils.getIterableComponentType(collectionType)
            when {
              type != null -> addType(type, expression)
              useTypeScriptKeyofType(collectionType) -> addType(
                JSCompositeTypeFactory.createIndexedAccessType(collectionType,
                                                               JSCompositeTypeFactory.createKeyOfType(
                                                                 collectionType, collectionType.source),
                                                               collectionType.source),
                expression)
              else -> addVForVarType(
                collectionExpr, *getComponentTypeFromArrayExpression(expression, collectionExpr).toTypedArray())
            }
          }
        }
        restoreEvaluationContextApplingElementsSize(myContext.jsElementsToApply.size)
        myContext.finishEvaluationWithStrictness(myContext.isStrict)
      }
      1 -> {
        val collectionType = JSResolveUtil.getElementJSType(collectionExpr)?.substitute()
        if (collectionType == null || JSTypeUtils.isAnyType(collectionType)) {
          addVForVarType(collectionExpr, ::JSStringType, ::JSNumberType)
        }
        else if (JSTypeUtils.isArrayLikeType(collectionType) || collectionType is JSPrimitiveType) {
          addVForVarType(collectionExpr, ::JSNumberType)
        }
        else {
          val recordType = collectionType.asRecordType()
          if (recordType.findPropertySignature(JSCommonTypeNames.ITERATOR_SYMBOL) != null) {
            addVForVarType(collectionExpr, ::JSNumberType)
          }
          else {
            val indexerTypes = JSRecordType.IndexSignatureKind.values()
              .asSequence()
              .mapNotNull { recordType.findIndexer(it) }
              .map { it.memberParameterType }
              .toList()

            when {
              indexerTypes.isNotEmpty() -> addVForVarType(collectionExpr, *indexerTypes.toTypedArray())
              useTypeScriptKeyofType(collectionType) -> addType(
                JSCompositeTypeFactory.createKeyOfType(collectionType,
                                                                                                                 collectionType.source),
                collectionExpr)
              else -> addVForVarType(collectionExpr, ::JSStringType, ::JSNumberType)
            }
          }
        }
      }
      2 -> addVForVarType(collectionExpr, ::JSNumberType)
    }
    return true
  }

  private fun addVForVarType(source: PsiElement, vararg types: (Boolean, JSTypeSource, JSTypeContext) -> JSType) {
    val typeSource = JSTypeSourceFactory.createTypeSource(source, false)
    addVForVarType(source, *types.map { it(true, typeSource, JSTypeContext.INSTANCE) }.toTypedArray())
  }

  private fun addVForVarType(source: PsiElement, vararg types: JSType) {
    val typeSource = JSTypeSourceFactory.createTypeSource(source, false)
    val commonType = (JSTupleTypeImpl(typeSource, types.toMutableList(), emptyList(), false, 0, false).toArrayType(
      false) as JSArrayType).type
    addType(commonType, source, true)
  }

  companion object {
    fun resolveEventType(@Suppress("UNUSED_PARAMETER") attribute: XmlAttribute): JSType? {
      // TODO resolve event type
      return null
    }

    private fun useTypeScriptKeyofType(collectionType: JSType): Boolean {
      return (collectionType.isTypeScript || collectionType.sourceElement?.language == VueJSLanguage.INSTANCE)
             && collectionType is JSRecordType
    }
  }
}
