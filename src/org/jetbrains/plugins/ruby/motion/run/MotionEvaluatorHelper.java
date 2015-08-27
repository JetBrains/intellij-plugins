package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.util.Pair;
import com.intellij.xdebugger.XSourcePosition;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.CidrEvaluatorHelper;
import com.jetbrains.cidr.execution.debugger.backend.DBCannotEvaluateException;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class MotionEvaluatorHelper extends CidrEvaluatorHelper {
  @Override
  public String convertExpression(@NotNull CidrDebugProcess process, String originalExpression, @Nullable XSourcePosition position) throws ConversionException {
    return originalExpression;
  }

  @Override
  public Pair<LLValue, String> convertAndEvaluate(@NotNull CidrDebugProcess process,
                                                  @NotNull DebuggerDriver driver,
                                                  @NotNull String expression,
                                                  XSourcePosition sourcePosition,
                                                  int frameNumber,
                                                  int threadId) throws ExecutionException, DBCannotEvaluateException {
    final LLValue result = driver.evaluate(threadId, frameNumber, expression);
    return Pair.create(result, expression);
  }
}
