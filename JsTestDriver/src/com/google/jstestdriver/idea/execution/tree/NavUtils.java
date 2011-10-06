package com.google.jstestdriver.idea.execution.tree;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.assertFramework.TestFileStructureManager;
import com.google.jstestdriver.idea.assertFramework.TestFileStructurePack;
import com.google.jstestdriver.idea.config.JstdConfigStructure;
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
import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class NavUtils {

  private NavUtils() {}

  @Nullable
  public static PsiElement findPsiElement(@NotNull Project project,
                                          @NotNull File jstdConfigFile,
                                          @NotNull String testCaseName,
                                          @Nullable String testMethodName) {
    JstdConfigStructure configStructure = JstdConfigStructure.newConfigStructure(jstdConfigFile);
    List<File> files = Lists.newArrayList();
    files.addAll(configStructure.getLoadFiles());
    files.addAll(configStructure.getTestFiles());
    for (File loadFile : files) {
      VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(loadFile);
      if (vf != null) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(vf);
        if (psiFile instanceof JSFile) {
          PsiElement element = findPsiElementInJsFile((JSFile)psiFile, testCaseName, testMethodName);
          if (element != null) {
            return element;
          }
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
