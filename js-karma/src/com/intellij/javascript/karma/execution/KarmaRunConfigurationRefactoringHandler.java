package com.intellij.javascript.karma.execution;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.UndoRefactoringElementAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaRunConfigurationRefactoringHandler {

  @Nullable
  public static RefactoringElementListener getRefactoringElementListener(@NotNull KarmaRunConfiguration configuration,
                                                                         @Nullable PsiElement element) {
    VirtualFile fileAtElement = toVirtualFile(element);
    if (fileAtElement == null) {
      return null;
    }
    KarmaRunSettings settings = configuration.getRunSettings();
    String path = fileAtElement.getPath();
    String configPath = FileUtil.toSystemIndependentName(settings.getConfigPath());
    if (configPath.equals(path)) {
      return new FilePathRefactoringElementListener(configuration);
    }
    return null;
  }

  @Nullable
  public static VirtualFile toVirtualFile(@Nullable PsiElement element) {
    if (element instanceof PsiFileSystemItem) {
      PsiFileSystemItem psiFileSystemItem = (PsiFileSystemItem) element;
      return psiFileSystemItem.getVirtualFile();
    }
    return null;
  }

  private static class FilePathRefactoringElementListener extends UndoRefactoringElementAdapter {
    private final KarmaRunConfiguration myConfiguration;

    private FilePathRefactoringElementListener(@NotNull KarmaRunConfiguration configuration) {
      myConfiguration = configuration;
    }

    @Override
    protected void refactored(@NotNull PsiElement element, @Nullable String oldQualifiedName) {
      VirtualFile newFile = toVirtualFile(element);
      if (newFile != null) {
        String newPath = FileUtil.toSystemDependentName(newFile.getPath());
        KarmaRunSettings.Builder builder = new KarmaRunSettings.Builder();
        builder.setConfigPath(newPath);
        myConfiguration.setRunSettings(builder.build());
      }
    }
  }

}
