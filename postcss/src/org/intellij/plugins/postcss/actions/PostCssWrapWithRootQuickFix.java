package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssCustomMixin;
import com.intellij.psi.css.CssElementFactory;
import com.intellij.psi.css.impl.CssElementTypes;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;

public class PostCssWrapWithRootQuickFix implements LocalQuickFix {
  @Override
  public @IntentionFamilyName @NotNull String getFamilyName() {
    return PostCssBundle.message("annotator.wrap.with.root.rule.quickfix.name");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement startElement = descriptor.getStartElement();
    if (startElement instanceof CssCustomMixin) {
      PsiElement nextSibling = startElement.getNextSibling();
      boolean isSemicolonAfter = nextSibling != null && nextSibling.getNode().getElementType() == CssElementTypes.CSS_SEMICOLON;
      if (isSemicolonAfter) {
        nextSibling.delete();
      }
      startElement
        .replace(CssElementFactory.getInstance(project).createRuleset(":root {" + startElement.getText() + "}", PostCssLanguage.INSTANCE));
    }
  }
}