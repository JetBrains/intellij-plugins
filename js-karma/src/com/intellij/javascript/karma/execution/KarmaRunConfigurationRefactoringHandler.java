package com.intellij.javascript.karma.execution;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.UndoRefactoringElementAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KarmaRunConfigurationRefactoringHandler {

  @Nullable
  public static RefactoringElementListener getRefactoringElementListener(@NotNull KarmaRunConfiguration configuration,
                                                                         @Nullable PsiElement element) {
    VirtualFile fileAtElement = PsiUtilBase.asVirtualFile(element);
    if (fileAtElement == null) {
      return null;
    }
    KarmaRunSettings settings = configuration.getRunSettings();
    String path = fileAtElement.getPath();
    String configPath = settings.getConfigPathSystemIndependent();
    if (configPath.equals(path)) {
      return new FilePathRefactoringElementListener(configuration);
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
      VirtualFile newFile = PsiUtilBase.asVirtualFile(element);
      if (newFile != null) {
        myConfiguration.setConfigFilePath(newFile.getPath());
      }
    }
  }

}
