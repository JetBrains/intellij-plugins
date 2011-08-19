package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.ObjectUtils;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.scripting.ScriptingLibraryModel;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.util.FileContentUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class JstdDefaultAssertionFrameworkSupportInspection extends JSInspection {

  private static final AddJstdLibraryLocalQuickFix ADD_JSTD_LIBRARY_LOCAL_QUICK_FIX = new AddJstdLibraryLocalQuickFix();

  @NotNull
  @Override
  public String getShortName() {
    return "JsTestDriverDefaultAssertionFrameworkSupport";
  }

  @Override
  protected PsiElementVisitor createVisitor(final ProblemsHolder holder, LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      @Override
      public void visitJSCallExpression(final JSCallExpression jsCallExpression) {
        JSReferenceExpression methodExpression = CastUtils.tryCast(jsCallExpression.getMethodExpression(), JSReferenceExpression.class);
        JSArgumentList jsArgumentList = jsCallExpression.getArgumentList();
        if (methodExpression != null && jsArgumentList != null) {
          JSExpression[] arguments = ObjectUtils.notNull(jsArgumentList.getArguments(), JSExpression.EMPTY_ARRAY);
          boolean suitableSymbol = isSuitableSymbol(methodExpression.getReferencedName(), arguments);
          if (suitableSymbol) {
            boolean resolved = canBeResolved(methodExpression);
            if (!resolved) {
              holder.registerProblem(
                  methodExpression,
                  getDisplayName(),
                  ProblemHighlightType.GENERIC_ERROR,
                  TextRange.create(0, methodExpression.getTextLength()),
                  ADD_JSTD_LIBRARY_LOCAL_QUICK_FIX
              );
            }
          }
        }
      }
    };
  }

  private static boolean isSuitableSymbol(String methodName, JSExpression[] arguments) {
    if (arguments.length < 1) {
      return false;
    }
    if (!JsPsiUtils.isStringElement(arguments[0])) {
      return false;
    }
    if ("TestCase".equals(methodName) || "AsyncTestCase".equals(methodName)) {
      if (arguments.length == 1) {
        return true;
      }
      if (arguments.length == 2 && JsPsiUtils.isObjectElement(arguments[1])) {
        return true;
      }
    }
    return false;
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return ADD_JSTD_LIBRARY_LOCAL_QUICK_FIX.getName();
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

  private static class AddJstdLibraryLocalQuickFix implements LocalQuickFix {

    @NotNull
    @Override
    public String getName() {
      return "Add JsTestDriver assertion framework support";
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      PsiElement psiElement = descriptor.getPsiElement();
      final ScriptingLibraryModel scriptingLibraryModel = JsAssertFrameworkLibraryManager.createScriptingLibraryModelAndAssociateIt(
        project, project.getBaseDir()
      );
      FileContentUtil.reparseFiles(project, Arrays.asList(psiElement.getContainingFile().getVirtualFile()), true);
      if (scriptingLibraryModel == null) {
        System.out.println("Unable to create library '" + JsAssertFrameworkLibraryManager.LIBRARY_NAME + "'");
      }
    }
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

}
