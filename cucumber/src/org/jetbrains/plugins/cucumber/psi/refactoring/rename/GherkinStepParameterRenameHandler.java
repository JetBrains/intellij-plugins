// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.refactoring.rename;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.PsiElementRenameHandler;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinStepParameter;
import org.jetbrains.plugins.cucumber.psi.GherkinTableCell;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

@NotNullByDefault
public class GherkinStepParameterRenameHandler extends PsiElementRenameHandler {
  @Override
  public boolean isAvailableOnDataContext(DataContext dataContext) {
    return getGherkinStepParameter(dataContext) != null;
  }

  @Override
  public void invoke(Project project, Editor editor, PsiFile file, DataContext dataContext) {
    GherkinStepParameter stepParameter = getGherkinStepParameter(dataContext);
    if (stepParameter == null) {
      return;
    }
    PsiReference stepParameterReference = stepParameter.getReference();
    if (stepParameterReference == null) {
      return;
    }
    if (!(stepParameterReference.resolve() instanceof GherkinTableCell tableCell)) {
      return;
    }
    super.invoke(project, new PsiElement[]{tableCell}, dataContext);
  }

  private static @Nullable GherkinStepParameter getGherkinStepParameter(@Nullable DataContext context) {
    PsiElement element = null;
    if (context == null) {
      return null;
    }
    final Editor editor = CommonDataKeys.EDITOR.getData(context);
    if (editor != null) {
      final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(context);
      if (psiFile != null) {
        element = psiFile.findElementAt(editor.getCaretModel().getOffset());
      }
    }
    if (element == null) {
      element = CommonDataKeys.PSI_ELEMENT.getData(context);
    }
    return PsiTreeUtil.getParentOfType(element, GherkinStepParameter.class, false);
  }
}
