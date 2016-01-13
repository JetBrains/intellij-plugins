package org.jetbrains.plugins.ruby.motion.run.renderers;

import com.intellij.execution.ExecutionException;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerSettings;
import com.jetbrains.cidr.execution.debugger.backend.DBCannotEvaluateException;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrPhysicalValue;
import com.jetbrains.cidr.execution.debugger.evaluation.CustomValueRendererFactory;
import com.jetbrains.cidr.execution.debugger.evaluation.EvaluationContext;
import com.jetbrains.cidr.execution.debugger.evaluation.renderers.NSCollectionValueRenderer;
import com.jetbrains.cidr.execution.debugger.evaluation.renderers.ValueRenderer;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class MotionValueRendererFactory implements CustomValueRendererFactory {
  @Nullable
  @Override
  public ValueRenderer createRenderer(CidrDebuggerSettings settings,
                                      CidrPhysicalValue value,
                                      LLValue var,
                                      EvaluationContext context) throws ExecutionException {
    try {
      if (var.isValidPointer()) {
        context.evaluate(EvaluationContext.cast("rb_inspect(" + EvaluationContext.cast(var.getPointer(), "id") + ")", "id"));
        // we want ObjC renderers for our collections
        // Ruby collections are inheriting from native ones and don't have any useful instance variables
        final ValueRenderer collectionRenderer = NSCollectionValueRenderer.createIfNSCollection(context, value, var);
        return collectionRenderer != null ? collectionRenderer : new MotionObjectRenderer(value);
      }
    } catch (DBCannotEvaluateException ignored) {}
    return null;
  }
}
