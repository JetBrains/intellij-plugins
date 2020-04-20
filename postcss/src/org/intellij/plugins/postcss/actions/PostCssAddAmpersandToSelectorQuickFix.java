package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssSelector;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssElementGenerator;
import org.jetbrains.annotations.NotNull;

public class PostCssAddAmpersandToSelectorQuickFix implements LocalQuickFix {
  @Override
  public @IntentionFamilyName @NotNull String getFamilyName() {
    return PostCssBundle.message("annotator.add.ampersand.quickfix.name");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement startElement = descriptor.getStartElement();
    if (startElement instanceof CssSelector) {
      CssSelector selector = (CssSelector)startElement;
      if (selector.getSimpleSelectors().length == 0) return;
      selector.addBefore(PostCssElementGenerator.createAmpersandSelector(project), selector.getSimpleSelectors()[0]);
    }
  }
}