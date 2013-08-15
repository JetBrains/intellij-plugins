package org.jetbrains.plugins.cucumber.refactoring;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStepParameter;
import org.jetbrains.plugins.cucumber.psi.GherkinTableCell;

/**
 * User: Andrey.Vokin
 * Date: 8/29/11
 */
public class GherkinRefactoringSupportProvider extends RefactoringSupportProvider {
  @Override
  public boolean isInplaceRenameAvailable(@NotNull PsiElement element, PsiElement context) {
    if (element instanceof GherkinStepParameter || element instanceof GherkinTableCell) {
      return true;
    }
    return false;
  }
}
