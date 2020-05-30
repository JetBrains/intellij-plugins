// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.refactoring;

import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.rename.RenameUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewTypeLocation;
import com.intellij.util.IncorrectOperationException;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSDirectiveRenameProcessor extends JSDefaultRenameProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    return DirectiveUtil.getDirective(element) != null;
  }

  @Override
  public @Nullable PsiElement substituteElementToRename(@NotNull PsiElement element, @Nullable Editor editor) {
    return DirectiveUtil.getDirective(element);
  }

  @Override
  public void renameElement(@NotNull PsiElement element,
                            @NotNull String newName,
                            UsageInfo @NotNull [] usages,
                            @Nullable RefactoringElementListener listener) throws IncorrectOperationException {
    final PsiNamedElement directive = (PsiNamedElement)element;
    final String attributeName;
    final String directiveName;
    if (newName.contains("-")) {
      attributeName = newName;
      directiveName = DirectiveUtil.normalizeAttributeName(newName);
    }
    else {
      attributeName = DirectiveUtil.getAttributeName(newName);
      directiveName = newName;
    }
    for (UsageInfo usage : usages) {
      RenameUtil.rename(usage, attributeName);
    }
    directive.setName(directiveName);
    if (listener != null) {
      listener.elementRenamed(element);
    }
  }

  @Override
  public @NotNull RenameDialog createRenameDialog(@NotNull Project project,
                                                  final @NotNull PsiElement element,
                                                  PsiElement nameSuggestionContext,
                                                  Editor editor) {
    final String directiveName = ((PsiNamedElement)element).getName();
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

  public static class AngularJSDirectiveElementDescriptor implements ElementDescriptionProvider {
    @Override
    public @Nullable String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
      JSImplicitElement directive = DirectiveUtil.getDirective(element);
      if (directive != null) {
        if (location instanceof UsageViewTypeLocation) return "directive";
        return directive.getName();
      }
      return null;
    }
  }
}
