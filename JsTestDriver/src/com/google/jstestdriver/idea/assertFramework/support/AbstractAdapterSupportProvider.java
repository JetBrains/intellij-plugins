package com.google.jstestdriver.idea.assertFramework.support;

import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractAdapterSupportProvider extends JSInspection {

  private final AdapterFix myAdapterQuickFix;

  protected AbstractAdapterSupportProvider() {
    myAdapterQuickFix = new AdapterFix(this);
  }

  public abstract String getAssertFrameworkName();

  public abstract List<VirtualFile> getAdapterSourceFiles();

  @NotNull
  @Override
  public String getShortName() {
    return getAssertFrameworkName() + "AssertionFrameworkAdapterSupportForJsTestDriver";
  }

  @NotNull
  public String getDisplayName() {
    return myAdapterQuickFix.getText();
  }

  protected abstract boolean isNeededSymbol(String methodName, JSExpression[] arguments);

  protected JSElementVisitor createVisitor(final ProblemsHolder holder, LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      @Override
      public void visitJSCallExpression(final JSCallExpression jsCallExpression) {
        JSReferenceExpression methodExpression = CastUtils.tryCast(jsCallExpression.getMethodExpression(), JSReferenceExpression.class);
        JSArgumentList jsArgumentList = jsCallExpression.getArgumentList();
        if (methodExpression != null && jsArgumentList != null) {
          JSExpression[] arguments = ObjectUtils.notNull(jsArgumentList.getArguments(), JSExpression.EMPTY_ARRAY);
          boolean subject = isNeededSymbol(methodExpression.getReferencedName(), arguments);
          if (subject) {
            boolean resolved = canBeResolved(methodExpression);
            if (!resolved) {
              holder.registerProblem(
                  methodExpression,
                  getDisplayName(),
                  ProblemHighlightType.GENERIC_ERROR,
                  TextRange.create(0, methodExpression.getTextLength()),
                  myAdapterQuickFix
              );
            }
          }
        }
      }
    };
  }

  private static boolean canBeResolved(PsiPolyVariantReference psiPolyVariantReference) {
    ResolveResult[] resolveResults = psiPolyVariantReference.multiResolve(false);
    for (ResolveResult resolveResult : resolveResults) {
      PsiElement resolvedElement = resolveResult.getElement();
      if (resolvedElement != null && resolveResult.isValidResult()) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

}
