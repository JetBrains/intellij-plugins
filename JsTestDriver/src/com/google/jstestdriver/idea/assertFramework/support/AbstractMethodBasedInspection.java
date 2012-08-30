package com.google.jstestdriver.idea.assertFramework.support;

import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public abstract class AbstractMethodBasedInspection extends JSInspection {

  protected abstract boolean isSuitableMethod(@NotNull String methodName, @NotNull JSExpression[] methodArguments);

  protected abstract IntentionAction getFix();

  protected abstract String getProblemDescription();

  @NotNull
  @Override
  protected final JSElementVisitor createVisitor(final ProblemsHolder holder, LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      @Override
      public void visitJSCallExpression(final JSCallExpression jsCallExpression) {
        JSReferenceExpression methodExpression = ObjectUtils.tryCast(jsCallExpression.getMethodExpression(), JSReferenceExpression.class);
        JSArgumentList jsArgumentList = jsCallExpression.getArgumentList();
        if (methodExpression != null && jsArgumentList != null) {
          JSExpression[] arguments = ObjectUtils.notNull(jsArgumentList.getArguments(), JSExpression.EMPTY_ARRAY);
          String methodName = methodExpression.getReferencedName();
          if (methodName == null) {
            return;
          }
          boolean suitableSymbol = isSuitableMethod(methodName, arguments);
          if (suitableSymbol) {
            boolean resolved = isResolved(methodExpression);
            if (!resolved) {
              TextRange rangeInElement = TextRange.create(0, methodExpression.getTextLength());
              HintWrapperQuickFix fix = new HintWrapperQuickFix(
                methodExpression,
                rangeInElement,
                getFix()
              );
              holder.registerProblem(
                methodExpression,
                getProblemDescription(),
                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                rangeInElement,
                fix
              );
            }
          }
        }
      }
    };
  }

  protected boolean isResolved(JSReferenceExpression methodExpression) {
    return JsPsiUtils.isResolvedToFunction(methodExpression);
  }

}
