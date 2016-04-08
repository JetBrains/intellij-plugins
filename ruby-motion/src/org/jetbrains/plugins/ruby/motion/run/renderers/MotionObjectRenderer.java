package org.jetbrains.plugins.ruby.motion.run.renderers;

import com.intellij.execution.ExecutionException;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.jetbrains.cidr.execution.debugger.backend.DBCannotEvaluateException;
import com.jetbrains.cidr.execution.debugger.backend.DBUserException;
import com.jetbrains.cidr.execution.debugger.backend.LLValue;
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
  protected String doComputeValue(@NotNull EvaluationContext context) throws ExecutionException, DBUserException {
    return TextUtil.removeQuoting(
      context.evaluate("(char *)[[(id)rb_inspect(" + myValue.getVar().getPointer() + ") description] UTF8String]").getReadableValue());
  }

  @Override
  protected boolean mayHaveChildrenViaChildrenCount() {
    return true;
  }

  @Override
  @Nullable
  protected Integer doComputeChildrenCount(@NotNull EvaluationContext context) throws ExecutionException, DBUserException {
    return count(context, getInstanceVariablesNames(context));
  }

  private static int count(EvaluationContext context, LLValue instanceVariablesNames) throws ExecutionException, DBUserException {
    return (int)context.evaluate(context.castIDToNumber("[" + getSelf(instanceVariablesNames) + " count]", "unsigned int")).intValue();
  }

  private LLValue getInstanceVariablesNames(EvaluationContext context) throws ExecutionException, DBCannotEvaluateException {
    return context.evaluate(EvaluationContext.cast("rb_obj_instance_variables(" + getSelf() + ")", "id"));
  }

  @Override
  protected void doComputeChildren(@NotNull EvaluationContext context,
                                   @NotNull XCompositeNode container) throws ExecutionException, DBUserException{

    final Collection<CidrValue> children = new ArrayList<CidrValue>();
    final LLValue names = getInstanceVariablesNames(context);
    final int count = count(context, names);
    for (int i = 0; i < count; i++) {
      final String selName = EvaluationContext.cast("sel_registerName(\"objectAtIndex:\")", "id");
      final String nameExpr = EvaluationContext.cast("objc_msgSend(" + getSelf(names) + ", " + selName + ", " + i + ")", "id");
      final LLValue name = context.evaluate(nameExpr);
      final String namePointer = "(char *)[[" + name.getPointer() + " description] UTF8String]";
      final String ivarName = TextUtil.removeQuoting(context.evaluate(namePointer).getReadableValue());
      final String ivarExpr = getChildEvaluationExpression(ivarName);
      final LLValue ivar = context.evaluate(ivarExpr);
      children.add(new MotionMemberValue(ivar, 
                                         ivarName, 
                                         myValue
      ));
    }
    CidrValue.addAllTo(children, container);
  }

  private String getChildEvaluationExpression(String ivarName) {
    final String ivarNameExpr = EvaluationContext.cast("rb_intern(\"" + ivarName + "\")", "char *");
    return EvaluationContext.cast("rb_ivar_get(" + getSelf() + ", " + ivarNameExpr + ")", "id");
  }

  @Override
  protected boolean shouldPrintChildrenConsoleDescription() {
    return true;
  }

  private String getSelf() {
    return EvaluationContext.cast(myValue.getVar().getPointerSafely(), "id");
  }

  private static String getSelf(LLValue value) throws DBCannotEvaluateException {
    return EvaluationContext.cast(value.getPointer(), "id");
  }
}
