package com.google.jstestdriver.idea.execution.tree;

import com.google.jstestdriver.idea.assertFramework.TestFileStructureManager;
import com.google.jstestdriver.idea.assertFramework.TestFileStructurePack;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class NavUtils {

  private NavUtils() {}

  @Nullable
  public static PsiElement findPsiElement(@NotNull Project project,
                                          @NotNull File jsTestFile,
                                          @NotNull String testCaseName,
                                          @Nullable String testMethodName) {
    if (!jsTestFile.isFile()) {
      return null;
    }
    VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(jsTestFile);
    if (vf != null) {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
      if (psiFile instanceof JSFile) {
        PsiElement element = findPsiElementInJsFile((JSFile)psiFile, testCaseName, testMethodName);
        if (element != null) {
          return element;
        }
      }
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
