package com.jetbrains.lang.dart.ide.runner.base;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfigurationBase;
import com.intellij.execution.configurations.RefactoringListenerProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DartRunConfigurationBase extends LocatableConfigurationBase implements RefactoringListenerProvider {

  private final RefactoringElementListener myRefactoringElementListener = new RefactoringElementListener() {
    @Override
    public void elementMoved(@NotNull final PsiElement newElement) {
      elementChanged(newElement);
    }

    protected void elementChanged(final @NotNull PsiElement newElement) {
      filePathChanged(newElement.getContainingFile().getVirtualFile());
    }

    @Override
    public void elementRenamed(@NotNull final PsiElement newElement) {
      elementChanged(newElement);
    }
  };

  protected DartRunConfigurationBase(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  protected abstract void filePathChanged(final @NotNull VirtualFile file);

  @Nullable
  @Override
  public RefactoringElementListener getRefactoringElementListener(final PsiElement element) {
    return myRefactoringElementListener;
  }

}
