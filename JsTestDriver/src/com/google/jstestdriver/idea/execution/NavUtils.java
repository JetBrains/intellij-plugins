package com.google.jstestdriver.idea.execution;

import com.intellij.javascript.testFramework.TestFileStructureManager;
import com.intellij.javascript.testFramework.TestFileStructurePack;
import com.intellij.javascript.testFramework.util.TestMethodNameRefiner;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class NavUtils {

  private NavUtils() {
  }

  @Nullable
  public static PsiElement findPsiLocation(@NotNull Project project,
                                           @NotNull VirtualFile jsTestVirtualFile,
                                           @NotNull String testCaseName,
                                           @Nullable String testMethodName,
                                           @Nullable TestMethodNameRefiner testMethodNameRefiner) {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(jsTestVirtualFile);
    if (!(psiFile instanceof JSFile)) {
      return null;
    }
    JSFile jsFile = (JSFile) psiFile;
    TestFileStructurePack pack = TestFileStructureManager.fetchTestFileStructurePackByJsFile(jsFile);
    if (pack != null) {
      return pack.findPsiElement(testCaseName, testMethodName, testMethodNameRefiner);
    }
    return null;
  }

}
