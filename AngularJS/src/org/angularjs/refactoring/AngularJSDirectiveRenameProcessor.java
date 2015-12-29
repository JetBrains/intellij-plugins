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

  @Nullable
  @Override
  public PsiElement substituteElementToRename(PsiElement element, @Nullable Editor editor) {
    return DirectiveUtil.getDirective(element);
  }

  @Override
  public void renameElement(PsiElement element, String newName, UsageInfo[] usages, @Nullable RefactoringElementListener listener) throws IncorrectOperationException {
    final String attributeName = DirectiveUtil.getAttributeName(newName);
    for (UsageInfo usage : usages) {
      RenameUtil.rename(usage, attributeName);
    }

    ((PsiNamedElement)element).setName(DirectiveUtil.attributeToDirective(element.getProject(), newName));
    if (listener != null) {
      listener.elementRenamed(element);
    }
  }

  @Override
  public RenameDialog createRenameDialog(Project project, final PsiElement element, PsiElement nameSuggestionContext, Editor editor) {
    final String directiveName = DirectiveUtil.attributeToDirective(element.getProject(), ((PsiNamedElement)element).getName());
    return new RenameDialog(project, element, nameSuggestionContext, editor) {
      @Override
      public String[] getSuggestedNames() {
        return new String[] {directiveName};
      }

      @Override
      protected boolean areButtonsValid() {
        return true;
      }
    };
  }

  public static class AngularJSDirectiveElementDescriptor implements ElementDescriptionProvider {
    @Nullable
    @Override
    public String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
      JSImplicitElement directive = DirectiveUtil.getDirective(element);
      if (directive != null) {
        if (location instanceof UsageViewTypeLocation) return "directive";
        return DirectiveUtil.attributeToDirective(directive.getProject(), directive.getName());
      }
      return null;
    }
  }
}
