package com.jetbrains.lang.dart.resolve;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.ResolveScopeManager;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class DartUseScope extends GlobalSearchScope {

  @NotNull final VirtualFile myFileWithSearchedDeclaration;

  /**
   * @param fileWithSearchedDeclaration must be within project content
   */
  public DartUseScope(@NotNull final Project project, @NotNull final VirtualFile fileWithSearchedDeclaration) {
    super(project);
    myFileWithSearchedDeclaration = fileWithSearchedDeclaration;
  }

  @Override
  public boolean contains(@NotNull VirtualFile file) {
    if (myFileWithSearchedDeclaration.equals(file)) return true;

    assert getProject() != null;
    final PsiFile psiFile = PsiManager.getInstance(getProject()).findFile(file);

    return psiFile != null &&
           ResolveScopeManager.getInstance(getProject()).getResolveScope(psiFile).contains(myFileWithSearchedDeclaration);
  }

  @Override
  public int compare(@NotNull VirtualFile file1, @NotNull VirtualFile file2) {
    return 0;
  }

  @Override
  public boolean isSearchInModuleContent(@NotNull Module aModule) {
    return true;
  }

  @Override
  public boolean isSearchInLibraries() {
    return false;
  }
}
