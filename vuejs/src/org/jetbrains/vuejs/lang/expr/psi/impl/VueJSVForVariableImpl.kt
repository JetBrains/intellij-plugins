// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor
import com.intellij.lang.javascript.psi.stubs.JSVariableStubBase
import com.intellij.lang.javascript.psi.types.*
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.expr.VueExprMetaLanguage
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForVariable

class VueJSVForVariableImpl(node: ASTNode) : JSVariableImpl<JSVariableStubBase<JSVariable>, JSVariable>(node), VueJSVForVariable, JSEvaluableElement {

  override fun hasBlockScope(): Boolean = true

  override fun calculateType(): JSType? {
    return PsiTreeUtil.getParentOfType(this, VueJSVForExpression::class.java)
      ?.getVarStatement()
      ?.declarations
      ?.takeIf { it.indexOf(this) in 0..2 }
      ?.let { JSPsiBasedTypeOfType(this, false) }
  }

  override fun getDeclarationScope(): PsiElement? =
    PsiTreeUtil.getContextOfType(this, XmlTag::class.java, PsiFile::class.java)

  override fun evaluate(evaluateContext: JSEvaluateContext, typeProcessor: JSTypeProcessor): Boolean {
    val vForExpression = PsiTreeUtil.getParentOfType(this, VueJSVForExpression::class.java) ?: return false

    val collectionExpr = vForExpression.getCollectionExpression() ?: return false

    val declaration = PsiTreeUtil.getTopmostParentOfType(this, JSInitializerOwner::class.java) ?: this

    when (vForExpression.getVarStatement()?.declarations?.indexOf(declaration)) {
      0 -> {
        val destructuringParents = JSTypeEvaluator.findDestructuringParents(this)
        val expression = evaluateContext.processedExpression
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
              else -> {
                val typeEvaluator = JSDialectSpecificHandlersFactory.forElement(collectionExpr).newTypeEvaluator(evaluateContext)
                val componentTypeFromArrayExpression = typeEvaluator.getComponentTypeFromArrayExpression(expression, collectionExpr)
                getVForVarType(collectionExpr, *componentTypeFromArrayExpression.toTypedArray())
              }
            }
          }
          else -> null
        }
        if (type != null) {
          val typeToAdd = destructuringParents.applyToOuterType(type)
          if (typeToAdd != null) typeProcessor.process(typeToAdd, evaluateContext)
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
        if (type != null) typeProcessor.process(type, evaluateContext)
      }
      2 -> {
        val type = getVForVarType(collectionExpr, ::JSNumberType)
        if (type != null) typeProcessor.process(type, evaluateContext)
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
    return (JSTupleTypeImpl(typeSource, types.toMutableList(), emptyList(), false, 0, false).toArrayType(false) as JSArrayType).type
  }

  private fun useTypeScriptKeyofType(collectionType: JSType): Boolean {
    return (collectionType.isTypeScript || VueExprMetaLanguage.matches(collectionType.sourceElement?.language)) // hmm
           && collectionType is JSRecordType
  }
}
