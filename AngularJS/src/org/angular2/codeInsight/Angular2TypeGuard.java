// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight;

import com.intellij.codeInsight.controlflow.Instruction;
import com.intellij.lang.javascript.evaluation.JSCodeBasedTypeFactory;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.controlflow.JSControlFlowService;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType;
import com.intellij.lang.javascript.psi.types.guard.JSInjectionControlFlowUtil;
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeGuard;
import com.intellij.lang.javascript.psi.types.guard.operations.JSNarrowTypeByTypePredicateOperation;
import com.intellij.lang.javascript.psi.types.guard.operations.JSTypeOperation;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Angular2TypeGuard extends TypeScriptTypeGuard {
  public Angular2TypeGuard(@NotNull PsiElement element) {
    super(element);
  }

  @Override
  protected @Nullable JSTypeOperation getDialectSpecificTypeOperation(@NotNull JSControlFlowService.JSControlFlow flow,
                                                                      @NotNull Instruction _instruction) {
    if (_instruction instanceof Angular2ControlFlowBuilder.Angular2ConditionInstruction instruction) {

      if (instruction.getElement() instanceof JSExpression element) {
        var guard = instruction.getCustomGuard();
        if (guard != null && myElement instanceof JSReferenceExpression referenceExpression && !referenceExpression.hasQualifier()) {
          JSTypeOperation prevOp = getPrevFlowType(flow, instruction);
          boolean assumeTrue = instruction.getValue();
          return narrowByCustomGuard(prevOp, assumeTrue, element, guard);
        }
      }
    }

    return null;
  }

  @NotNull
  private static JSNarrowTypeByTypePredicateOperation narrowByCustomGuard(@NotNull JSTypeOperation prevOp,
                                                                         boolean assumeTrue,
                                                                         @NotNull JSExpression expression,
                                                                         @NotNull JSElement guard) {
    IntOpenHashSet matchedArguments = new IntOpenHashSet();
    matchedArguments.add(1);
    IntOpenHashSet matchedNonRefs = new IntOpenHashSet();
    Map<Integer, JSTypeOperation> operations = new HashMap<>();

    var typeSource = JSTypeSourceFactory.createTypeSource(expression, true);
    var evaluateContext = new JSEvaluateContext(expression.getContainingFile());

    var baseType = JSCodeBasedTypeFactory.getPsiBasedType(guard, evaluateContext);
    var argumentTypes = ContainerUtil.immutableList(
      JSAnyType.get(typeSource),
      JSResolveUtil.getExpressionJSType(expression)
    );
    var type = new JSApplyCallType(baseType, argumentTypes, typeSource);

    return new JSNarrowTypeByTypePredicateOperation(prevOp, type, matchedArguments, matchedNonRefs, assumeTrue, operations, false);
  }
}
