package com.jetbrains.lang.dart.ide.runner.base;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RefactoringListenerProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.UndoRefactoringElementAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartRunConfigurationBase extends LocatableConfigurationBase implements RefactoringListenerProvider {

  private final RefactoringElementListener myRefactoringElementListener = new UndoRefactoringElementAdapter() {
    protected void refactored(@NotNull final PsiElement element, @Nullable final String oldQualifiedName) {
      final boolean generatedName = getName().equals(suggestedName());

      setFilePath(element.getContainingFile().getVirtualFile().getPath());

      if (generatedName) {
        setGeneratedName();
      }
    }
  };

  protected DartRunConfigurationBase(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  protected abstract void setFilePath(final @NotNull String path);

  @Nullable
  public abstract String getFilePath();

  @Nullable
  @Override
  public RefactoringElementListener getRefactoringElementListener(final PsiElement element) {
    if (element instanceof PsiFile) {
      final String filePath = getFilePath();
      final VirtualFile changedFile = ((PsiFile)element).getVirtualFile();
      if (filePath != null && changedFile != null && filePath.equals(changedFile.getPath())) {
        return myRefactoringElementListener;
      }
    }
    return null;
  }
}
