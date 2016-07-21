package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.PostCssElementGenerator;
import org.jetbrains.annotations.NotNull;

public class PostCssAddPrefixToCustomSelectorQuickFix extends LocalQuickFixBase {
  public PostCssAddPrefixToCustomSelectorQuickFix() {
    super(PostCssBundle.message("annotator.add.prefix.to.custom.selector.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement startElement = descriptor.getStartElement();
    if (startElement instanceof PostCssCustomSelector) {
      PostCssCustomSelector customSelector = (PostCssCustomSelector)startElement;
      String customSelectorText = customSelector.getText();
      for (int i = 0; i < 3; i++) {
        if (!StringUtil.startsWith(customSelectorText, ":--".substring(0, i + 1))) {
          customSelectorText = ":--" + customSelectorText.substring(i);
          break;
        }
      }
      customSelector.replace(PostCssElementGenerator.createCustomSelector(project, customSelectorText));
    }
  }
}