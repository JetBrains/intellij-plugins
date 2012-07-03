package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.jstd.jsSrc.JstdDefaultAssertionFrameworkSrcMarker;
import com.google.jstestdriver.idea.assertFramework.library.JstdLibraryUtil;
import com.google.jstestdriver.idea.assertFramework.support.AbstractMethodBasedInspection;
import com.google.jstestdriver.idea.assertFramework.support.ChooseScopeAndCreateLibraryDialog;
import com.google.jstestdriver.idea.util.JsPsiUtils;
import com.google.jstestdriver.idea.util.VfsUtils;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class JstdAssertionFrameworkSupportInspection extends AbstractMethodBasedInspection {

  private static final AddJstdLibraryLocalQuickFix ADD_JSTD_LIBRARY_LOCAL_QUICK_FIX = new AddJstdLibraryLocalQuickFix();

  @Override
  protected boolean isSuitableMethod(@NotNull String methodName, @NotNull JSExpression[] methodArguments) {
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
  protected String getProblemDescription() {
    return "No code assistance for JsTestDriver assertion framework";
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
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          List<VirtualFile> sources = getLibrarySourceFiles();
          final VirtualFile fileRequestor = findVirtualFile(descriptor.getPsiElement());
          DialogWrapper dialog = new ChooseScopeAndCreateLibraryDialog(
            project,
            JstdLibraryUtil.LIBRARY_NAME,
            sources,
            fileRequestor,
            false
          );
          AsyncResult<Boolean> result = dialog.showAndGetOk();
          result.doWhenDone(new AsyncResult.Handler<Boolean>() {
            @Override
            public void run(Boolean done) {
              if (done) {
                FileContentUtil.reparseFiles(project, Collections.singletonList(fileRequestor), true);
              }
            }
          });
        }
      });
    }

    @NotNull
    private static List<VirtualFile> getLibrarySourceFiles() {
      return VfsUtils.findVirtualFilesByResourceNames(
        JstdDefaultAssertionFrameworkSrcMarker.class,
        new String[]{"Asserts.js", "TestCase.js"}
      );
    }

    @Nullable
    private static VirtualFile findVirtualFile(@Nullable PsiElement element) {
      if (element != null) {
        PsiFile file = element.getContainingFile();
        if (file != null) {
          return file.getVirtualFile();
        }
      }
      return null;
    }

  }

}
