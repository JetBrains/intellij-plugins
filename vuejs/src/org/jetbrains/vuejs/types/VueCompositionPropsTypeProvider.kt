// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.types

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.typescript.resolve.TypeScriptTypeHelper
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.asSafely
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.source.DATA_PROP
import org.jetbrains.vuejs.model.source.SETUP_METHOD

object VueCompositionPropsTypeProvider {
  fun addTypeFromResolveResult(evaluator: JSTypeEvaluator, result: PsiElement): Boolean {
    // JSThisType is not resolved to a Vue Component in JS
    if (result is JSProperty &&
        DialectDetector.isJavaScript(result) &&
        isVueContext(result) &&
        PsiTreeUtil.getContextOfType(result, true, JSProperty::class.java)?.name == DATA_PROP &&
        VueModelManager.findEnclosingComponent(result) != null) {
      val propertyType = result.value?.let { TypeScriptTypeHelper.getInstance().getTypeForIndexing(it, result) }
      if (propertyType != null) {
        evaluator.addType(propertyType)
        return true
      }
    }

    if (result is JSParameter && PsiTreeUtil.getStubChildOfType(result.context, JSParameter::class.java) == result) {
      val method = result.context?.let { (it as? JSParameterList)?.context ?: it }?.asSafely<JSFunction>() ?: return false
      val initializer = method.context
      if (method.name != SETUP_METHOD
          || initializer !is JSObjectLiteralExpression
          || PsiTreeUtil.getContextOfType(initializer, true, JSClass::class.java, JSObjectLiteralExpression::class.java) != null
          || !isVueContext(method)
      ) return false
      VueModelManager.getComponent(initializer)
        ?.let {
          evaluator.addType(VuePropsType(it))
          return true
        }
    }
    return false
  }

  fun useOnlyCompleteMatch(type: JSType): Boolean = type is VueCompleteType
}