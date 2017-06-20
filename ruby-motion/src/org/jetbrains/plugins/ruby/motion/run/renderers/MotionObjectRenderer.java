/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.motion.run.renderers;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.util.Pair;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XFullValueEvaluator;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerCommandException;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.backend.LLValueData;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrValue;
import com.jetbrains.cidr.execution.debugger.evaluation.EvaluationContext;
import com.jetbrains.cidr.execution.debugger.evaluation.renderers.ValueRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.motion.run.MotionMemberValue;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Dennis.Ushakov
 */
public class MotionObjectRenderer extends ValueRenderer {

  public MotionObjectRenderer(@NotNull CidrPhysicalValue value) {
    super(value);
  }

  @NotNull
  @Override
  protected Pair<String, XFullValueEvaluator> doComputeValueAndEvaluator(@NotNull EvaluationContext context) throws ExecutionException,
                                                                                                                    DebuggerCommandException {
    LLValue value =
      context.evaluate("(char *)[[(id)rb_inspect(" + myValue.getVarData(context).getPointer() + ") description] UTF8String]");
    LLValueData data = context.getData(value);
    return doComputeValueAndEvaluator(context, value, data);
  }

  @Override
  protected boolean mayHaveChildrenViaChildrenCount() {
    return true;
  }

  @Override
  @Nullable
  protected Integer doComputeChildrenCount(@NotNull EvaluationContext context) throws ExecutionException, DebuggerCommandException {
    return count(context, getInstanceVariablesNames(context));
  }

  private static int count(EvaluationContext context, LLValue instanceVariablesNames) throws ExecutionException, DebuggerCommandException {
    return (int)context.evaluateData(context.castIDToNumber("[" + getSelf(instanceVariablesNames, context) + " count]", "unsigned int")).intValue();
  }

  private LLValue getInstanceVariablesNames(EvaluationContext context) throws ExecutionException, DebuggerCommandException {
    return context.evaluate(EvaluationContext.cast("rb_obj_instance_variables(" + getSelf(context) + ")", "id"));
  }

  @Override
  protected void doComputeChildren(@NotNull EvaluationContext context,
                                   @NotNull XCompositeNode container) throws ExecutionException, DebuggerCommandException {

    final Collection<CidrValue> children = new ArrayList<>();
    final LLValue names = getInstanceVariablesNames(context);
    final int count = count(context, names);
    for (int i = 0; i < count; i++) {
      final String selName = EvaluationContext.cast("sel_registerName(\"objectAtIndex:\")", "id");
      final String nameExpr = EvaluationContext.cast("objc_msgSend(" + getSelf(names, context) + ", " + selName + ", " + i + ")", "id");
      final LLValueData name = context.evaluateData(nameExpr);
      final String namePointer = "(char *)[[" + name.getPointer() + " description] UTF8String]";
      final String ivarName = TextUtil.removeQuoting(context.evaluateData(namePointer).getPresentableValue());
      final String ivarExpr = getChildEvaluationExpression(context, ivarName);
      final LLValue ivar = context.evaluate(ivarExpr);
      children.add(new MotionMemberValue(ivar,
                                         ivarName,
                                         myValue
      ));
    }
    CidrValue.addAllTo(children, container);
  }

  private String getChildEvaluationExpression(@NotNull EvaluationContext context, String ivarName)
    throws ExecutionException, DebuggerCommandException {
    final String ivarNameExpr = EvaluationContext.cast("rb_intern(\"" + ivarName + "\")", "char *");
    return EvaluationContext.cast("rb_ivar_get(" + getSelf(context) + ", " + ivarNameExpr + ")", "id");
  }

  private String getSelf(@NotNull EvaluationContext context) throws ExecutionException, DebuggerCommandException {
    return EvaluationContext.cast(myValue.getVarData(context).getPointer(), "id");
  }

  private static String getSelf(LLValue value, @NotNull EvaluationContext context) throws DebuggerCommandException, ExecutionException {
    return EvaluationContext.cast(context.getData(value).getPointer(), "id");
  }
}
