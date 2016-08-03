package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssElementFactory;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.jetbrains.annotations.NotNull;

public class PostCssDeleteAmpersandQuickFix extends LocalQuickFixBase {
  public PostCssDeleteAmpersandQuickFix() {
    super(PostCssBundle.message("annotator.delete.ampersand.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getStartElement();
    CssSelectorList selectorList = PsiTreeUtil.getParentOfType(element, CssSelectorList.class);
    if (PostCssPsiUtil.isAmpersand(element) && selectorList != null) {
      element.delete();
      selectorList.replace(
        CssElementFactory.getInstance(element.getProject()).createSelectorList(selectorList.getText(), PostCssLanguage.INSTANCE));
    }
  }
}