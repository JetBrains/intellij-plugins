package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.jstd.jsSrc.JstdDefaultAssertionFrameworkSrcMarker;
import com.google.jstestdriver.idea.assertFramework.library.JsLibraryHelper;
import com.google.jstestdriver.idea.assertFramework.library.JstdLibraryUtil;
import com.google.jstestdriver.idea.assertFramework.support.AbstractMethodBasedInspection;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.project.Project;
import com.intellij.webcore.libraries.ScriptingLibraryModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class JstdAssertionFrameworkSupportInspection extends AbstractMethodBasedInspection {

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

  @Override
  protected boolean isResolved(@NotNull JSReferenceExpression methodExpression) {
    if (JsPsiUtils.isResolvedToFunction(methodExpression)) {
      return true;
    }
    PsiFile psiFile = methodExpression.getContainingFile();
    if (psiFile != null) {
      VirtualFile virtualFile = psiFile.getVirtualFile();
      if (virtualFile != null) {
        return JstdLibraryUtil.isFileInJstdLibScope(methodExpression.getProject(), virtualFile);
      }
    }
    return true;
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
      installLibrary(project);
      PsiElement psiElement = descriptor.getPsiElement();
      PsiFile psiFile = psiElement.getContainingFile();
      VirtualFile virtualFile = psiFile.getVirtualFile();

      if (virtualFile != null) {
        FileContentUtil.reparseFiles(project, Collections.singletonList(virtualFile), true);
      }
    }

    private static void installLibrary(@NotNull Project project) {
      List<VirtualFile> sources = getLibrarySourceFiles();
      JsLibraryHelper libraryHelper = new JsLibraryHelper(project);
      ScriptingLibraryModel libraryModel = libraryHelper.createJsLibrary(JstdLibraryUtil.LIBRARY_NAME, sources);
      String dialogTitle = "Adding JsTestDriver assertion framework support";
      if (libraryModel == null) {
        Messages.showErrorDialog("Unable to create '" + JstdLibraryUtil.LIBRARY_NAME + "' JavaScript library", dialogTitle);
        return;
      }
      boolean associated = libraryHelper.associateLibraryWithProject(libraryModel);
      if (!associated) {
        Messages.showErrorDialog("Unable to associate '" + JstdLibraryUtil.LIBRARY_NAME
                                 + "' JavaScript library with project", dialogTitle);
      }
    }
  }

  private static List<VirtualFile> getLibrarySourceFiles() {
    return VfsUtils.findVirtualFilesByResourceNames(
      JstdDefaultAssertionFrameworkSrcMarker.class,
      new String[]{"Asserts.js", "TestCase.js"}
    );
  }
}
