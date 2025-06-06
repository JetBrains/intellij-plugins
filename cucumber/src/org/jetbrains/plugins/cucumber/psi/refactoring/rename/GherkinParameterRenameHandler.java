// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

/**
 * Handles renaming Gherkin parameters – both usages ({@link GherkinStepParameter}) and declarations ({@link GherkinTableCell}).
 * <p>
 * Parameters can only be used inside <i>Scenario Outlines</i>.
 *
 * @see <a href="https://cucumber.io/docs/gherkin/reference#scenario-outline">Gherkin Reference | Scenario Outline</a>
 */
public final class GherkinParameterRenameHandler extends VariableInplaceRenameHandler {
  @Override
  protected boolean isAvailable(@Nullable PsiElement element, @NotNull Editor editor, @NotNull PsiFile file) {
    return element instanceof GherkinStepParameter || element instanceof GherkinTableCell;
  }

  @Override
  protected @NotNull VariableInplaceRenamer createRenamer(@NotNull PsiElement elementToRename, @NotNull Editor editor) {
    if (!(elementToRename instanceof PsiNamedElement namedElementToRename)) {
      throw new IllegalArgumentException("elementToRename must be PsiNamedElement, but was: " + elementToRename.getClass());
    }
    return new GherkinInplaceRenamer(namedElementToRename, editor);
  }
}
