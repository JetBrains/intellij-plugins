package org.jetbrains.plugins.cucumber.psi.refactoring.rename;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler;
import com.intellij.refactoring.rename.inplace.VariableInplaceRenamer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinStepParameter;
import org.jetbrains.plugins.cucumber.psi.GherkinTableCell;

public class GherkinInplaceRenameHandler extends VariableInplaceRenameHandler {
  @Override
  protected boolean isAvailable(PsiElement element, Editor editor, PsiFile file) {
    return element instanceof GherkinStepParameter || element instanceof GherkinTableCell;
  }

  @Nullable
  @Override
  protected VariableInplaceRenamer createRenamer(@NotNull PsiElement elementToRename, Editor editor) {
    return new GherkinInplaceRenamer((PsiNamedElement)elementToRename, editor);
  }
}
