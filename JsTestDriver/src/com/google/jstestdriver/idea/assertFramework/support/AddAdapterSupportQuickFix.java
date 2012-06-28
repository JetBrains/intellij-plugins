package com.google.jstestdriver.idea.assertFramework.support;

import com.google.inject.Provider;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.FileContentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

class AddAdapterSupportQuickFix implements LocalQuickFix {

  private final String myAssertionFrameworkName;
  private final Provider<List<VirtualFile>> myAdapterSourceFilesProvider;
  private final String myAdapterHomePageUrl;

  public AddAdapterSupportQuickFix(@NotNull String assertionFrameworkName,
                                   @NotNull Provider<List<VirtualFile>> adapterSourceFilesProvider,
                                   @Nullable String adapterHomePageUrl) {
    myAssertionFrameworkName = assertionFrameworkName;
    myAdapterSourceFilesProvider = adapterSourceFilesProvider;
    myAdapterHomePageUrl = adapterHomePageUrl;
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getName();
  }

  @NotNull
  @Override
  public String getName() {
    return "Add " + myAssertionFrameworkName + " JsTestDriver adapter";
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiFile psiFile = descriptor.getPsiElement().getContainingFile();
    if (psiFile != null) {
      doIt(project, psiFile);
    }
  }

  public void doIt(@NotNull final Project project, @NotNull final PsiFile psiFile) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        AddAdapterSupportDialog dialog = new AddAdapterSupportDialog(
          project,
          psiFile,
          myAssertionFrameworkName,
          myAdapterSourceFilesProvider.get(),
          myAdapterHomePageUrl
        );
        final AsyncResult<Boolean> result = dialog.showAndGetOk();
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile != null) {
          result.doWhenDone(new AsyncResult.Handler<Boolean>() {
            @Override
            public void run(Boolean ok) {
              if (ok) {
                FileContentUtil.reparseFiles(project, Arrays.<VirtualFile>asList(virtualFile), true);
              }
            }
          });
        }
      }
    });
  }

}
