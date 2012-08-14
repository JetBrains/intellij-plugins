package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.assertFramework.TestFileStructureManager;
import com.google.jstestdriver.idea.assertFramework.TestFileStructurePack;
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
                                           @Nullable String testMethodName) {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(jsTestVirtualFile);
    if (psiFile instanceof JSFile) {
      PsiElement element = findPsiElementInJsFile((JSFile)psiFile, testCaseName, testMethodName);
      return element;
    }
    return null;
  }

  @Nullable
  private static PsiElement findPsiElementInJsFile(@NotNull JSFile jsFile,
                                                   @NotNull String testCaseName,
                                                   @Nullable String testMethodName) {
    TestFileStructurePack pack = TestFileStructureManager.fetchTestFileStructurePackByJsFile(jsFile);
    if (pack != null) {
      return pack.findPsiElement(testCaseName, testMethodName);
    }
    return null;
  }
}
