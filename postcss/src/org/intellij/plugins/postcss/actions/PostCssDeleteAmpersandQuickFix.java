package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssElementFactory;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;

public class PostCssDeleteAmpersandQuickFix implements LocalQuickFix {
  @Override
  public @IntentionFamilyName @NotNull String getFamilyName() {
    return PostCssBundle.message("annotator.delete.ampersand.quickfix.name");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getStartElement();
    if (element != null) {
      String newText = descriptor.getTextRangeInElement().replace(element.getText(), "");
      element.replace(CssElementFactory.getInstance(element.getProject()).createSelectorList(newText, PostCssLanguage.INSTANCE));
    }
  }
}