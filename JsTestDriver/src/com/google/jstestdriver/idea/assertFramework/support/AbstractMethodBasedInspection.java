package com.google.jstestdriver.idea.assertFramework.support;

import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public abstract class AbstractMethodBasedInspection extends JSInspection {

  protected abstract boolean isSuitableMethod(String methodName, JSExpression[] methodArguments);

  protected abstract LocalQuickFix getQuickFix();

  @Override
  protected final JSElementVisitor createVisitor(final ProblemsHolder holder, LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      @Override
      public void visitJSCallExpression(final JSCallExpression jsCallExpression) {
        JSReferenceExpression methodExpression = CastUtils.tryCast(jsCallExpression.getMethodExpression(), JSReferenceExpression.class);
        JSArgumentList jsArgumentList = jsCallExpression.getArgumentList();
        if (methodExpression != null && jsArgumentList != null) {
          JSExpression[] arguments = ObjectUtils.notNull(jsArgumentList.getArguments(), JSExpression.EMPTY_ARRAY);
          boolean suitableSymbol = isSuitableMethod(methodExpression.getReferencedName(), arguments);
          if (suitableSymbol) {
            boolean resolved = isResolved(methodExpression);
            if (!resolved) {
              holder.registerProblem(
                methodExpression,
                getDisplayName(),
                ProblemHighlightType.GENERIC_ERROR,
                TextRange.create(0, methodExpression.getTextLength()),
                getQuickFix()
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

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @Nls
  @NotNull
  @Override
  public final String getDisplayName() {
    return getQuickFix().getName();
  }
}
