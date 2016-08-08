package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssElementFactory;
import com.intellij.psi.css.impl.CssElementTypes;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;

public class PostCssAddPrefixToCustomMediaQuickFix extends LocalQuickFixBase {
  public PostCssAddPrefixToCustomMediaQuickFix() {
    super(PostCssBundle.message("annotator.add.prefix.to.custom.media.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement startElement = descriptor.getStartElement();
    if (startElement.getNode().getElementType() == CssElementTypes.CSS_IDENT) {
      String customMediaText = startElement.getText();
      for (int i = 0; i < 2; i++) {
        if (!StringUtil.startsWith(customMediaText, "--".substring(0, i + 1))) {
          customMediaText = "--" + customMediaText.substring(i);
          break;
        }
      }
      startElement.replace(CssElementFactory.getInstance(project).createToken(customMediaText, PostCssLanguage.INSTANCE));
    }
  }
}