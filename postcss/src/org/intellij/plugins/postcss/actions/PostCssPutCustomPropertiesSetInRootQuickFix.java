package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssCustomMixin;
import com.intellij.psi.css.CssElementFactory;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;

public class PostCssPutCustomPropertiesSetInRootQuickFix extends LocalQuickFixBase {
  public PostCssPutCustomPropertiesSetInRootQuickFix() {
    super(PostCssBundle.message("annotator.wrap.custom.property.set.with.root.rule.quickfix.name"));
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement startElement = descriptor.getStartElement();
    if (startElement instanceof CssCustomMixin) {
      startElement
        .replace(CssElementFactory.getInstance(project).createRuleset(":root {" + startElement.getText() + "}", PostCssLanguage.INSTANCE));
    }
  }
}