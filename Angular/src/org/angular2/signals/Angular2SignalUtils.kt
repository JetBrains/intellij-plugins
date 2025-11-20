// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.signals

import com.intellij.javascript.web.js.WebJSResolveUtil
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSCompositeTypeFactory
import com.intellij.lang.javascript.psi.types.JSGenericTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeComparingContextService.LOCATION
import com.intellij.lang.javascript.psi.types.JSUnionOrIntersectionType
import com.intellij.lang.javascript.psi.types.recordImpl.ComputedPropertySignatureImpl
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil
import org.angular2.Angular2DecoratorUtil.INPUT_DEC
import org.angular2.Angular2DecoratorUtil.OUTPUT_DEC
import org.angular2.index.getFunctionNameFromIndex
import org.angular2.isFromImportedPackage
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_PACKAGE
import org.angular2.lang.Angular2LangUtil.ANGULAR_CORE_RX_INTEROP_PACKAGE

object Angular2SignalUtils {

  const val SIGNAL_TYPE: String = "Signal"
  const val INPUT_FUN: String = "input"
  const val MODEL_FUN: String = "model"
  const val OUTPUT_FUN: String = "output"
  const val WRITABLE_SIGNAL_TYPE: String = "WritableSignal"
  const val SIGNAL_FUNCTION: String = "signal"
  const val OUTPUT_FROM_OBSERVABLE_FUN: String = "outputFromObservable"

  const val VIEW_CHILD_FUN: String = "viewChild"
  const val VIEW_CHILDREN_FUN: String = "viewChildren"
  const val REQUIRED_FUN: String = "required"

  fun signalTypeAlias(context: PsiElement?): TypeScriptTypeAlias? =
    WebJSResolveUtil.resolveSymbolFromNodeModule(
      context, Angular2LangUtil.ANGULAR_CORE_PACKAGE, SIGNAL_TYPE,
      TypeScriptTypeAlias::class.java
    )

  fun inputFunction(context: PsiElement?): TypeScriptVariable? =
    WebJSResolveUtil.resolveSymbolFromNodeModule(
      context, Angular2LangUtil.ANGULAR_CORE_PACKAGE, INPUT_FUN,
      TypeScriptVariable::class.java
    )

  fun modelFunction(context: PsiElement?): TypeScriptVariable? =
    WebJSResolveUtil.resolveSymbolFromNodeModule(
      context, Angular2LangUtil.ANGULAR_CORE_PACKAGE, MODEL_FUN,
      TypeScriptVariable::class.java
    )

  fun writableSignalInterface(context: PsiElement?): TypeScriptInterface? =
    WebJSResolveUtil.resolveSymbolFromNodeModule(
      context, Angular2LangUtil.ANGULAR_CORE_PACKAGE, WRITABLE_SIGNAL_TYPE,
      TypeScriptInterface::class.java
    )

  fun signalFunction(context: PsiElement?): TypeScriptFunction? =
    WebJSResolveUtil.resolveSymbolFromNodeModule(
      context, Angular2LangUtil.ANGULAR_CORE_PACKAGE, SIGNAL_FUNCTION,
      TypeScriptFunction::class.java
    )

  fun supportsSignals(context: PsiElement?): Boolean =
    signalTypeAlias(context) != null

  fun supportsInputSignals(context: PsiElement?): Boolean =
    inputFunction(context) != null

  fun supportsModels(context: PsiElement?): Boolean =
    modelFunction(context) != null

  fun isSignal(targetElement: PsiElement?, place: PsiElement?, writable: Boolean = false): Boolean {
    if (targetElement == null) return false
    JSTypeEvaluationLocationProvider.assertLocationIsSet()
    val signalTypeAlias = if (writable) writableSignalInterface(targetElement) else signalTypeAlias(targetElement)
    if (signalTypeAlias == targetElement) return false

    val signalType = signalTypeAlias
      ?.jsType
      ?.let { JSGenericTypeImpl(it.source, it, JSAnyType.get(it.source)) }
    if (signalType != null) {
      val toCheck = place ?: targetElement
      val elementType =
        if (toCheck is JSExpression) {
          JSResolveUtil.getExpressionJSType(toCheck)
        }
        else {
          JSResolveUtil.getElementJSType(toCheck)
        }
          ?.substitute(toCheck)
          ?.let {
            JSCompositeTypeFactory.optimizeTypeIfComposite(it, JSUnionOrIntersectionType.OptimizedKind.OPTIMIZED_REMOVED_NULL_UNDEFINED)
          }
      if (elementType != null
          && elementType.asRecordType(targetElement.containingFile).findPropertySignature("SIGNAL") is ComputedPropertySignatureImpl
          && signalType.isDirectlyAssignableType(elementType, ProcessingContext().apply { put(LOCATION, toCheck) })
      ) {
        return true
      }
    }
    return false
  }

  fun isDirectiveSignalInputOrOutput(psiElement: PsiElement?): Boolean =
    psiElement.asSafely<TypeScriptField>()
      ?.initializerOrStub
      ?.asSafely<JSCallExpression>()
      ?.let { isSignalInputOutFunctionCall(it) } == true

  fun isSignalInputOutFunctionCall(call: JSCallExpression): Boolean =
    getFunctionNameFromIndex(call)
      ?.split('.')
      ?.takeIf { it.size == 1 || it.size == 2 && it[1] == REQUIRED_FUN }
      ?.get(0)
      ?.takeIf { it == INPUT_FUN || it == OUTPUT_FUN || it == MODEL_FUN || it == OUTPUT_FROM_OBSERVABLE_FUN }
      ?.let {
        isFromImportedPackage(it, call, ANGULAR_CORE_PACKAGE, ANGULAR_CORE_RX_INTEROP_PACKAGE)
      } == true

  fun addWritableSignal(context: PsiElement?, propertyType: JSType): JSType {
    val signal = writableSignalInterface(context)?.jsType
                 ?: return propertyType
    val source = propertyType.source
    return JSCompositeTypeFactory.getCommonType(
      propertyType, JSGenericTypeImpl(source, signal, propertyType),
      source, true
    )
  }

  @StubUnsafe
  fun getPossibleSignalFunNameForLiteralParameter(expression: JSLiteralExpression): Pair<String, String?>? =
    expression
      .parent?.asSafely<JSArgumentList>()
      ?.parent?.asSafely<JSCallExpression>()
      ?.methodExpression?.asSafely<JSReferenceExpression>()?.let { expr ->
        val qualifier = expr.qualifier
        if (qualifier is JSReferenceExpression) {
          if (qualifier.hasQualifier())
            null
          else
            Pair(qualifier.referenceName ?: return@let null, expr.referenceName)
        }
        else if (qualifier == null) {
          expr.referenceName?.let { Pair(it, null) }
        }
        else null
      }
      ?.takeIf { Angular2LangUtil.isAngular2Context(expression) }

  fun isViewChildSignalCall(methodCallInfo: Pair<String, String?>?): Boolean =
    methodCallInfo?.first == VIEW_CHILD_FUN && methodCallInfo.second.let { it == null || it == REQUIRED_FUN }

  fun isViewChildrenSignalCall(methodCallInfo: Pair<String, String?>?): Boolean =
    methodCallInfo?.first == VIEW_CHILDREN_FUN && methodCallInfo.second == null

}

