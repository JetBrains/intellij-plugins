package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.support.AbstractMethodBasedInspection;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.scripting.ScriptingLibraryModel;
import com.intellij.psi.PsiElement;
import com.intellij.util.FileContentUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class JstdAssertionFrameworkSupportInspection extends AbstractMethodBasedInspection {

  private static final Logger LOG = Logger.getInstance(JstdAssertionFrameworkSupportInspection.class);
  private static final AddJstdLibraryLocalQuickFix ADD_JSTD_LIBRARY_LOCAL_QUICK_FIX = new AddJstdLibraryLocalQuickFix();

  @Override
  protected boolean isSuitableMethod(String methodName, JSExpression[] methodArguments) {
    if (methodArguments.length < 1) {
      return false;
    }
    if (!JsPsiUtils.isStringElement(methodArguments[0])) {
      return false;
    }
    if ("TestCase".equals(methodName) || "AsyncTestCase".equals(methodName)) {
      if (methodArguments.length == 1) {
        return true;
      }
      if (methodArguments.length == 2 && JsPsiUtils.isObjectElement(methodArguments[1])) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected LocalQuickFix getQuickFix() {
    return ADD_JSTD_LIBRARY_LOCAL_QUICK_FIX;
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
        LOG.warn("Unable to create library '" + JsAssertFrameworkLibraryManager.LIBRARY_NAME + "'");
      }
    }
  }

}
