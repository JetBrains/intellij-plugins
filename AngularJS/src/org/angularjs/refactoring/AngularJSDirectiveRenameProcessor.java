package org.angularjs.refactoring;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.rename.RenameUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewTypeLocation;
import com.intellij.usageView.UsageViewUtil;
import com.intellij.util.IncorrectOperationException;
import org.angularjs.codeInsight.DirectiveUtil;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSDirectiveRenameProcessor extends JSDefaultRenameProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    return isDirective(element);
  }

  @Nullable
  @Override
  public PsiElement substituteElementToRename(PsiElement element, @Nullable Editor editor) {
    if (element instanceof JSLiteralExpression && ((JSLiteralExpression)element).isQuotedLiteral()) {
      final JSNamedElementProxy directive = getDirective(element, StringUtil.unquoteString(element.getText()));
      if (directive != null) return directive;
    }
    if (element instanceof JSNamedElementProxy) {
      return element;
    }
    return null;
  }

  private static boolean isDirective(PsiElement element) {
    if (element instanceof JSNamedElementProxy) {
      if (getDirective(element, ((JSNamedElementProxy)element).getName()) != null) return true;
    }
    if (element instanceof JSLiteralExpression && ((JSLiteralExpression)element).isQuotedLiteral()) {
      if (getDirective(element, StringUtil.unquoteString(element.getText())) != null) return true;
    }
    return false;
  }

  private static JSNamedElementProxy getDirective(PsiElement element, final String name) {
    final String directiveName = DirectiveUtil.getAttributeName(name);
    final JSNamedElementProxy directive = AngularIndexUtil.resolve(element.getProject(), AngularDirectivesIndex.INDEX_ID, directiveName);
    if (directive != null && element.getTextRange().contains(directive.getTextOffset())) {
      return directive;
    }
    return null;
  }

  @Override
  public void renameElement(PsiElement element, String newName, UsageInfo[] usages, @Nullable RefactoringElementListener listener) throws IncorrectOperationException {
    final String attributeName = DirectiveUtil.getAttributeName(newName);
    for (UsageInfo usage : usages) {
      RenameUtil.rename(usage, attributeName);
    }

    ((PsiNamedElement)element).setName(newName);
    if (listener != null) {
      listener.elementRenamed(element);
    }
  }

  @Override
  public RenameDialog createRenameDialog(Project project, final PsiElement element, PsiElement nameSuggestionContext, Editor editor) {
    final String directiveName = DirectiveUtil.attributeToDirective(((PsiNamedElement)element).getName());
    return new RenameDialog(project, element, nameSuggestionContext, editor) {
      @NotNull
      @Override
      protected String getLabelText() {
        return RefactoringBundle.message("rename.0.and.its.usages.to", UsageViewUtil.getType(element) + " " + directiveName);
      }

      @Override
      public String[] getSuggestedNames() {
        return new String[] {directiveName};
      }
    };
  }

  public static class AngularJSDirectiveElementDescriptor implements ElementDescriptionProvider {
    @Nullable
    @Override
    public String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
      if (isDirective(element)) {
        if (location instanceof UsageViewTypeLocation) return "directive";
        return DirectiveUtil.attributeToDirective(((JSNamedElementProxy)element).getName());
      }
      return null;
    }
  }
}
