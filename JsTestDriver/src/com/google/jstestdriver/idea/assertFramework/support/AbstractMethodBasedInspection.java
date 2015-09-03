package com.google.jstestdriver.idea.assertFramework.support;

import com.google.jstestdriver.idea.execution.JstdSettingsUtil;
import com.google.jstestdriver.idea.util.JstdResolveUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMethodBasedInspection extends JSInspection {

  @Override
  public boolean isEnabledByDefault() {
    return false;
  }

  protected abstract boolean isSuitableElement(@NotNull JSFile jsFile, @NotNull JSCallExpression callExpression);

  protected abstract IntentionAction getFix();

  protected abstract String getProblemDescription();

  @NotNull
  @Override
  protected final JSElementVisitor createVisitor(final ProblemsHolder holder, LocalInspectionToolSession session) {
    if (holder == null) {
      return JSElementVisitor.NOP_ELEMENT_VISITOR;
    }
    Project project = holder.getProject();
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      if (!JstdSettingsUtil.areJstdConfigFilesInProjectCached(project)) {
        return JSElementVisitor.NOP_ELEMENT_VISITOR;
      }
    }
    return new JSElementVisitor() {
      @Override
      public void visitJSCallExpression(final JSCallExpression jsCallExpression) {
        JSFile jsFile = null;
        if (jsCallExpression != null) {
          jsFile = ObjectUtils.tryCast(jsCallExpression.getContainingFile(), JSFile.class);
        }
        if (jsFile == null) {
          return;
        }
        JSReferenceExpression methodExpression = ObjectUtils.tryCast(jsCallExpression.getMethodExpression(), JSReferenceExpression.class);
        if (methodExpression == null) {
          return;
        }
        boolean suitableSymbol = isSuitableElement(jsFile, jsCallExpression);
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
    };
  }

  protected boolean isResolved(JSReferenceExpression methodExpression) {
    return JstdResolveUtil.isResolvedToFunction(methodExpression);
  }

}
