package com.jetbrains.lang.dart.ide.runner.base;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RefactoringListenerProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.UndoRefactoringElementAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartRunConfigurationBase extends LocatableConfigurationBase implements RefactoringListenerProvider {

  private class RenameRefactoringListener extends UndoRefactoringElementAdapter {

    private final String myOldPath;

    private RenameRefactoringListener(@NotNull String oldPath) {
      myOldPath = oldPath;
    }

    @SuppressWarnings("ConstantConditions")
    private String getNewPath(@NotNull PsiElement newElement) {
      // File path cannot be null
      return getFilePath().replaceFirst(myOldPath, getPath(newElement));
    }

    private @NotNull String getPath(PsiElement element) {
      final VirtualFile virtualFile = ((PsiFileSystemItem)element).getVirtualFile();
      if (virtualFile != null) {
        return virtualFile.getPath();
      }
      return ""; //Shouldn't happen
    }

    @Override
    protected void refactored(@NotNull final PsiElement element, @Nullable final String oldQualifiedName) {
      final boolean generatedName = getName().equals(suggestedName());

      setFilePath(getNewPath(element));

      if (generatedName) {
        setGeneratedName();
      }
    }
  }


  protected DartRunConfigurationBase(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  protected abstract void setFilePath(final @NotNull String path);

  @Nullable
  public abstract String getFilePath();

  @Nullable
  @Override
  public RefactoringElementListener getRefactoringElementListener(final PsiElement element) {

    if (element instanceof PsiFileSystemItem) {
      final String filePath = getFilePath();
      if (filePath != null) {
        final VirtualFile changedElement = ((PsiFileSystemItem)element).getVirtualFile();
        if (changedElement != null) {
          final String affectedPath = changedElement.getPath();
          if (element instanceof PsiFile) {
            if (filePath.equals(affectedPath)) {
              return new RenameRefactoringListener(affectedPath);
            }
          }
          if (element instanceof PsiDirectory) {
            if (filePath.startsWith(affectedPath)) {
              return new RenameRefactoringListener(affectedPath);
            }
          }
        }
      }
    }

    return null;
  }

}
