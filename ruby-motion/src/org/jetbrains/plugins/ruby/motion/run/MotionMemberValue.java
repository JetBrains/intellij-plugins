package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Dennis.Ushakov
*/
public class MotionMemberValue extends CidrPhysicalValue {
  private final CidrPhysicalValue myParent;

  public MotionMemberValue(final LLValue var,
                           final String displayName,
                           final CidrPhysicalValue parent) {
    super(var, displayName, parent.getProcess(), parent.getSourcePosition(), parent.getFrame());
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
    return myParent.getPreparedRenderer().getChildEvaluationExpression(this, lvalue);
  }
}
