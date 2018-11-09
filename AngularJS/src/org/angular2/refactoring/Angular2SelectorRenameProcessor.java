// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.usageView.UsageViewTypeLocation;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2SelectorRenameProcessor extends RenamePsiElementProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    return element instanceof Angular2DirectiveSelectorPsiElement;
  }

  @NotNull
  @Override
  public RenameDialog createRenameDialog(@NotNull Project project,
                                         @NotNull final PsiElement element,
                                         PsiElement nameSuggestionContext,
                                         Editor editor) {
    final String directiveName = DirectiveUtil.attributeToDirective(element, ((PsiNamedElement)element).getName());
    return new RenameDialog(project, element, nameSuggestionContext, editor) {
      @Override
      public String[] getSuggestedNames() {
        return new String[]{directiveName};
      }

      @Override
      protected boolean areButtonsValid() {
        return true;
      }
    };
  }

  public static class Angular2SelectorElementDescriptor implements ElementDescriptionProvider {
    @Nullable
    @Override
    public String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
      if (element instanceof Angular2DirectiveSelectorPsiElement) {
        if (location instanceof UsageViewTypeLocation) {
          return ((Angular2DirectiveSelectorPsiElement)element).isElementSelector()
                 ? "element selector"
                 : "attribute selector";
        }
        return ((Angular2DirectiveSelectorPsiElement)element).getName();
      }
      return null;
    }
  }
}
