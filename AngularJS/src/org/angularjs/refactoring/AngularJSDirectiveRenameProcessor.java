package org.angularjs.refactoring;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.usageView.UsageInfo;
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

  private static boolean isDirective(PsiElement element) {
    if (element instanceof JSNamedElementProxy) {
      if (isDirective(element, ((JSNamedElementProxy)element).getName())) return true;
    }
    if (element instanceof JSLiteralExpression && ((JSLiteralExpression)element).isQuotedLiteral()) {
      if (isDirective(element, StringUtil.unquoteString(element.getText()))) return true;
    }
    return false;
  }

  private static boolean isDirective(PsiElement element, final String name) {
    final String directiveName = DirectiveUtil.normalizeAttributeName(name);
    final JSNamedElementProxy directive = AngularIndexUtil.resolve(element.getProject(), AngularDirectivesIndex.INDEX_ID, directiveName);
    if (element.getTextRange().contains(directive.getTextOffset())) {
      return true;
    }
    return false;
  }

  @Override
  public void renameElement(PsiElement element, String newName, UsageInfo[] usages, @Nullable RefactoringElementListener listener) throws IncorrectOperationException {
    super.renameElement(element, newName, usages, listener);
  }

  @Override
  public RenameDialog createRenameDialog(Project project, final PsiElement element, PsiElement nameSuggestionContext, Editor editor) {
    final String directiveName = element instanceof PsiNamedElement ?
                                 DirectiveUtil.attributeToDirective(((PsiNamedElement)element).getName()) :
                                 StringUtil.unquoteString(element.getText());
    return new RenameDialog(project, element, nameSuggestionContext, editor) {
      @NotNull
      @Override
      protected String getLabelText() {
        return RefactoringBundle.message("rename.0.and.its.usages.to", "directive " + directiveName);
      }

      @Override
      public String[] getSuggestedNames() {
        return new String[] {directiveName};
      }
    };
  }
}
