package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XValueModifier;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.CidrStackFrame;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import com.jetbrains.cidr.execution.debugger.evaluation.EvaluationContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Dennis.Ushakov
*/
public class MotionMemberValue extends CidrPhysicalValue {
  private final CidrPhysicalValue myParent;

  public MotionMemberValue(final CidrDebugProcess process, EvaluationContext context,
                           final XSourcePosition position, final CidrStackFrame frame, final LLValue ivar,
                           final String ivarName, final CidrPhysicalValue parent) throws ExecutionException {
    super(process, context, position, frame, ivar, ivarName);
    myParent = parent;
  }

  @Nullable
  @Override
  protected XSourcePosition doComputePosition(@NotNull XSourcePosition position) {
    return null;
  }

  @NotNull
  @Override
  public String getEvaluationExpression(boolean lvalue) {
    return myParent.getRenderer().getChildEvaluationExpression(this, lvalue);
  }

  @Override
  public XValueModifier getModifier() {
    // Disable value editing for now
    return null;
  }
}
