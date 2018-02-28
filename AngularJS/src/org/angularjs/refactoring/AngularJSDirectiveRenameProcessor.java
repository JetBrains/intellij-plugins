package org.angularjs.refactoring;

import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenameDialog2;
import com.intellij.refactoring.rename.RenameUtil;
import com.intellij.refactoring.rename.ValidationResult;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewTypeLocation;
import com.intellij.util.IncorrectOperationException;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularJS2IndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

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
  public PsiElement substituteElementToRename(@NotNull PsiElement element, @Nullable Editor editor) {
    return DirectiveUtil.getDirective(element);
  }

  @Override
  public void renameElement(@NotNull PsiElement element, @NotNull String newName, @NotNull UsageInfo[] usages, @Nullable RefactoringElementListener listener) throws IncorrectOperationException {
    final boolean isAngular2 = DirectiveUtil.isAngular2Directive(element);
    final PsiNamedElement directive = (PsiNamedElement)element;
    final String attributeName = isAngular2 ? newName : DirectiveUtil.getAttributeName(newName);
    for (UsageInfo usage : usages) {
      RenameUtil.rename(usage, attributeName);
    }

    if (isAngular2) {
      final JSProperty selector = AngularJS2IndexingHandler.getSelector(element.getParent());
      final JSExpression value = selector != null ? selector.getValue() : null;
      if (value != null) {
        if (value.getText().contains("[")) newName = "[" + newName + "]";
        ElementManipulators.getManipulator(value).handleContentChange(value, newName);
      }
    } else {
      directive.setName(DirectiveUtil.attributeToDirective(element, newName));
    }
    if (listener != null) {
      listener.elementRenamed(element);
    }
  }

  @NotNull
  @Override
  public RenameDialog2 createRenameDialog2(@NotNull Project project, @NotNull final PsiElement element, PsiElement nameSuggestionContext, Editor editor) {
    RenameDialog2 d = super.createRenameDialog2(project, element, nameSuggestionContext, editor);
    d.setValidate(s -> new ValidationResult(true, null));
    final String directiveName = DirectiveUtil.attributeToDirective(element, ((PsiNamedElement)element).getName());
    d.setSuggestedNames(Collections.singletonList(directiveName));
    return d;
  }

  public static class AngularJSDirectiveElementDescriptor implements ElementDescriptionProvider {
    @Nullable
    @Override
    public String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
      JSImplicitElement directive = DirectiveUtil.getDirective(element);
      if (directive != null) {
        if (location instanceof UsageViewTypeLocation) return "directive";
        return DirectiveUtil.attributeToDirective(directive, directive.getName());
      }
      return null;
    }
  }
}
