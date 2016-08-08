package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.intellij.plugins.postcss.psi.PostCssElementGenerator;
import org.jetbrains.annotations.NotNull;

public class PostCssAddPrefixToCustomMediaQuickFix extends LocalQuickFixBase {
  public PostCssAddPrefixToCustomMediaQuickFix() {
    super(PostCssBundle.message("annotator.add.prefix.to.custom.media.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement startElement = descriptor.getStartElement();
    if (startElement instanceof PostCssCustomMedia) {
      String customMediaText = startElement.getText();
      for (int i = 0; i < 2; i++) {
        if (!StringUtil.startsWith(customMediaText, "--".substring(0, i + 1))) {
          customMediaText = "--" + customMediaText.substring(i);
          break;
        }
      }
      startElement.replace(ObjectUtils.notNull(PostCssElementGenerator.createCustomMedia(project, customMediaText)));
    }
  }
}