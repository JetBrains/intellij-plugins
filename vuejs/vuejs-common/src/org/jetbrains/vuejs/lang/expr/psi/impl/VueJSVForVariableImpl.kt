// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory
import com.intellij.lang.javascript.psi.JSCommonTypeNames
import com.intellij.lang.javascript.psi.JSEvaluableElement
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.JSInitializerOwner
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.impl.JSVariableImpl
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor
import com.intellij.lang.javascript.psi.stubs.JSVariableStubBase
import com.intellij.lang.javascript.psi.types.JSArrayType
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSIterableComponentTypeImpl
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType
import com.intellij.lang.javascript.psi.types.JSTupleTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.JSUnionType
import com.intellij.lang.javascript.psi.types.JSWidenType
import com.intellij.lang.javascript.psi.types.JSWrapperType
import com.intellij.lang.javascript.psi.types.primitives.JSNullType
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType
import com.intellij.lang.javascript.psi.types.primitives.JSObjectType
import com.intellij.lang.javascript.psi.types.primitives.JSPrimitiveType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.lang.javascript.psi.types.primitives.JSUndefinedType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.lang.expr.psi.VueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForVariable

class VueJSVForVariableImpl(node: ASTNode) :
  JSVariableImpl<JSVariableStubBase<JSVariable>, JSVariable>(node),
  VueJSVForVariable,
  JSEvaluableElement {

  override fun hasBlockScope(): Boolean = true

  override fun calculateType(): JSType? {
    return PsiTreeUtil.getParentOfType(this, VueJSVForExpression::class.java)
      ?.getVarStatement()
      ?.declarations
      ?.takeIf { it.indexOf(this) in 0..2 }
      ?.let { JSPsiBasedTypeOfType(this, false) }
  }

  override fun getDeclarationScope(): PsiElement? {
    var element: PsiElement = this
    val jsFile = (((parent as? JSVarStatement)
      ?.parent as? VueJSVForExpression)
      ?.parent as? VueJSEmbeddedExpressionContent)
      ?.parent as? JSFile
    if (jsFile != null) {
      val injectionHost = InjectedLanguageManager.getInstance(jsFile.project).getInjectionHost(jsFile)
      if (injectionHost is JSLiteralExpression) {
        element = injectionHost
      }
    }
    return PsiTreeUtil.getContextOfType(element, XmlTag::class.java, PsiFile::class.java)
  }

  override fun evaluate(evaluateContext: JSEvaluateContext, typeProcessor: JSTypeProcessor): Boolean {
    val vForExpression = PsiTreeUtil.getParentOfType(this, VueJSVForExpression::class.java) ?: return false

    val collectionExpr = vForExpression.getCollectionExpression() ?: return false

    val declaration = PsiTreeUtil.getTopmostParentOfType(this, JSInitializerOwner::class.java) ?: this

    when (vForExpression.getVarStatement()?.declarations?.indexOf(declaration)) {
      0 -> {
        val destructuringParents = JSTypeEvaluator.findDestructuringParents(this)
        val expression = evaluateContext.processedExpression
        val type = when (val collectionType = removeNullAndUndefinedFromUnion(JSResolveUtil.getElementJSType(collectionExpr)?.substitute())) {
          is JSStringType -> getVForVarType(collectionExpr, JSNamedTypeFactory::createStringPrimitiveType)
          is JSNumberType -> getVForVarType(collectionExpr, JSNamedTypeFactory::createNumberPrimitiveType)
          is JSType -> {
            val type = JSTypeUtils.getIterableComponentType(collectionType)
            when {
              type != null -> type
              collectionType is JSRecordType -> createIndexedAccessType(collectionType)
              else -> {
                val typeEvaluator = JSDialectSpecificHandlersFactory.forElement(collectionExpr).newTypeEvaluator(evaluateContext)
                val componentTypeFromArrayExpression = typeEvaluator.getComponentTypeFromArrayExpression(expression, collectionExpr)
                getVForVarType(collectionExpr, *componentTypeFromArrayExpression.map { preprocessItemType(it) }.toTypedArray())
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
        val collectionType = removeNullAndUndefinedFromUnion(JSResolveUtil.getElementJSType(collectionExpr)?.substitute())
        val type: JSType? = if (collectionType == null || JSTypeUtils.isAnyType(collectionType)) {
          getVForVarType(collectionExpr, JSNamedTypeFactory::createStringPrimitiveType, JSNamedTypeFactory::createNumberPrimitiveType)
        }
        else if (JSTypeUtils.isArrayLikeType(collectionType) ||
                 collectionType is JSPrimitiveType ||
                 (collectionType is JSUnionType &&
                  collectionType.types.all { JSTypeUtils.isArrayLikeType(it) || it is JSPrimitiveType })) {
          getVForVarType(collectionExpr, JSNamedTypeFactory::createNumberPrimitiveType)
        }
        else {
          val recordType = collectionType.asRecordType()
          if (recordType.findPropertySignature(JSCommonTypeNames.ITERATOR_SYMBOL) != null) {
            getVForVarType(collectionExpr, JSNamedTypeFactory::createNumberPrimitiveType)
          }
          else {
            val indexerTypes = recordType.indexSignatures.map { it.memberParameterType }

            when {
              indexerTypes.isNotEmpty() -> getVForVarType(collectionExpr, *indexerTypes.toTypedArray())
              useKeyOfForIndexParam(collectionType) -> JSCompositeTypeFactory.createKeyOfType(collectionType, collectionType.source)
              else -> getVForVarType(collectionExpr, JSNamedTypeFactory::createStringPrimitiveType, JSNamedTypeFactory::createNumberPrimitiveType)
            }
          }
        }
        if (type != null) typeProcessor.process(type, evaluateContext)
      }
      2 -> {
        val type = getVForVarType(collectionExpr, JSNamedTypeFactory::createNumberPrimitiveType)
        if (type != null) typeProcessor.process(type, evaluateContext)
      }
    }
    return true
  }

  private fun removeNullAndUndefinedFromUnion(type: JSType?): JSType? =
    if (type is JSUnionType)
      JSCompositeTypeFactory.createUnionType(
        type.source,
        type.types.filter { it !is JSUndefinedType && it !is JSNullType }
      )
    else
      type

  private fun createIndexedAccessType(collectionType: JSType): JSType {
    val keyOfType = JSCompositeTypeFactory.createKeyOfType(collectionType, collectionType.source)
    val indexedAccessType = JSCompositeTypeFactory.createIndexedAccessType(collectionType, keyOfType, collectionType.source)
    return JSWidenType.createWidening(indexedAccessType, null)
  }

  private fun getVForVarType(source: PsiElement, vararg types: (JSTypeSource) -> JSType): JSType? {
    val typeSource = JSTypeSourceFactory.createTypeSource(source, DialectDetector.isTypeScript(source))
    return getVForVarType(source, *types.map { it(typeSource) }.toTypedArray())
  }

  private fun getVForVarType(source: PsiElement, vararg types: JSType): JSType? {
    val typeSource = JSTypeSourceFactory.createTypeSource(source, DialectDetector.isTypeScript(source))
    val tupleType = JSTupleTypeImpl(typeSource, types.toMutableList(), emptyList(), false, 0, false)
    return (tupleType.toArrayType(false) as JSArrayType).type
  }

  private fun preprocessItemType(type: JSType): JSType {
    if (type !is JSIterableComponentTypeImpl) {
      return type
    }

    return when (val iterableType = type.iterableType) {
      is JSNumberType, is JSStringType -> iterableType
      is JSTypeImpl, is JSWrapperType -> createIndexedAccessType(iterableType.asRecordType())
      else -> type
    }
  }

  private fun useKeyOfForIndexParam(collectionType: JSType): Boolean =
    collectionType !is JSObjectType
}