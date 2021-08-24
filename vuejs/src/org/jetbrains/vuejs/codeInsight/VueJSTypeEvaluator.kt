// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
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

class VueJSTypeEvaluator(context: JSEvaluateContext)
  : TypeScriptTypeEvaluator(context) {

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
        val destructuringParents = findDestructuringParents(jsVariable)
        val expression = myContext.processedExpression
        val type = when (val collectionType = JSResolveUtil.getElementJSType(collectionExpr)?.substitute()) {
          is JSStringType -> getVForVarType(collectionExpr, ::JSStringType)
          is JSNumberType -> getVForVarType(collectionExpr, ::JSNumberType)
          is JSType -> {
            val type = JSTypeUtils.getIterableComponentType(collectionType)
            when {
              type != null -> type
              useTypeScriptKeyofType(collectionType) -> {
                val keyOfType = JSCompositeTypeFactory.createKeyOfType(collectionType, collectionType.source)
                val indexedAccessType = JSCompositeTypeFactory.createIndexedAccessType(collectionType, keyOfType, collectionType.source)
                JSWidenType.createWidening(indexedAccessType, null)
              }
              else -> getVForVarType(
                collectionExpr, *getComponentTypeFromArrayExpression(expression, collectionExpr).toTypedArray())
            }
          }
          else -> null
        }
        if (type != null) {
          val typeToAdd = destructuringParents.applyToOuterType(type)
          addType(typeToAdd)
        }
      }
      1 -> {
        val collectionType = JSResolveUtil.getElementJSType(collectionExpr)?.substitute()
        val type: JSType? = if (collectionType == null || JSTypeUtils.isAnyType(collectionType)) {
          getVForVarType(collectionExpr, ::JSStringType, ::JSNumberType)
        }
        else if (JSTypeUtils.isArrayLikeType(collectionType) || collectionType is JSPrimitiveType) {
          getVForVarType(collectionExpr, ::JSNumberType)
        }
        else {
          val recordType = collectionType.asRecordType()
          if (recordType.findPropertySignature(JSCommonTypeNames.ITERATOR_SYMBOL) != null) {
            getVForVarType(collectionExpr, ::JSNumberType)
          }
          else {
            val indexerTypes = recordType.indexSignatures.map { it.memberParameterType }

            when {
              indexerTypes.isNotEmpty() -> getVForVarType(collectionExpr, *indexerTypes.toTypedArray())
              useTypeScriptKeyofType(collectionType) ->
                JSCompositeTypeFactory.createKeyOfType(collectionType, collectionType.source)
              else -> getVForVarType(collectionExpr, ::JSStringType, ::JSNumberType)
            }
          }
        }
        addType(type)
      }
      2 -> {
        addType(getVForVarType(collectionExpr, ::JSNumberType))
      }
    }
    return true
  }

  private fun getVForVarType(source: PsiElement, vararg types: (Boolean, JSTypeSource, JSTypeContext) -> JSType): JSType? {
    val typeSource = JSTypeSourceFactory.createTypeSource(source, false)
    return getVForVarType(source, *types.map { it(true, typeSource, JSTypeContext.INSTANCE) }.toTypedArray())
  }

  private fun getVForVarType(source: PsiElement, vararg types: JSType): JSType? {
    val typeSource = JSTypeSourceFactory.createTypeSource(source, false)
    return (JSTupleTypeImpl(typeSource, types.toMutableList(), emptyList(), false, 0, false).toArrayType(
      false) as JSArrayType).type
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
