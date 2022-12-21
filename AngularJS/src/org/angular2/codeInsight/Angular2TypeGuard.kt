// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.controlflow.Instruction
import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeGuard
import com.intellij.lang.javascript.psi.types.guard.operations.JSNarrowTypeByTypePredicateOperation
import com.intellij.lang.javascript.psi.types.guard.operations.JSTypeOperation
import com.intellij.psi.PsiElement
import com.intellij.util.containers.ContainerUtil
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder

class Angular2TypeGuard(element: PsiElement) : TypeScriptTypeGuard(element) {

  override fun getDialectSpecificTypeOperation(flow: JSControlFlowService.JSControlFlow,
                                               instruction: Instruction): JSTypeOperation? {
    if (instruction is Angular2ControlFlowBuilder.Angular2ConditionInstruction) {
      val element = instruction.getElement()
      if (element is JSExpression) {
        val guard = instruction.customGuard
        if (guard != null && myElement.let { it is JSReferenceExpression && !it.hasQualifier() }) {
          val prevOp = getPrevFlowType(flow, instruction)
          val assumeTrue = instruction.value
          return narrowByCustomGuard(prevOp, assumeTrue, element, guard)
        }
      }
    }

    return null
  }

  private fun narrowByCustomGuard(prevOp: JSTypeOperation,
                                  assumeTrue: Boolean,
                                  expression: JSExpression,
                                  guard: JSElement): JSNarrowTypeByTypePredicateOperation {
    val matchedArguments = IntOpenHashSet()
    matchedArguments.add(1)
    val matchedNonRefs = IntOpenHashSet()
    val operations = HashMap<Int, JSTypeOperation>()

    val typeSource = JSTypeSourceFactory.createTypeSource(expression, true)
    val evaluateContext = JSEvaluateContext(expression.containingFile)

    val baseType = JSCodeBasedTypeFactory.getPsiBasedType(guard, evaluateContext)
    val argumentTypes = ContainerUtil.immutableList(
      JSAnyType.get(typeSource),
      JSResolveUtil.getExpressionJSType(expression)
    )
    val type = JSApplyCallType(baseType, argumentTypes, typeSource)

    return JSNarrowTypeByTypePredicateOperation(prevOp, type, matchedArguments, matchedNonRefs, assumeTrue, operations, false)
  }
}
