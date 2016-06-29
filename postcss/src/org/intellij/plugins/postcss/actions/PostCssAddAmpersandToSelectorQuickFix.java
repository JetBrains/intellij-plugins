package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.CssSelector;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssElementGenerator;
import org.intellij.plugins.postcss.psi.impl.PostCssDirectNestImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PostCssAddAmpersandToSelectorQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
  public PostCssAddAmpersandToSelectorQuickFix(@NotNull CssSelector selector) {
    super(selector);
  }

  @NotNull
  @Override
  public String getText() {
    return PostCssBundle.message("annotator.add.ampersand.quickfix.name");
  }

  @Nls
  @NotNull
  @Override
  public String getFamilyName() {
    return PostCssBundle.message("postcss.inspections.family");
  }

  @Override
  public void invoke(@NotNull Project project,
                     @NotNull PsiFile file,
                     @Nullable("is null when called from inspection") Editor editor,
                     @NotNull PsiElement startElement,
                     @NotNull PsiElement endElement) {
    PostCssDirectNestImpl ampersand = PostCssElementGenerator.createAmpersand(project);
    if (ampersand == null) return;
    if (startElement instanceof CssSelector) {
      CssSelector selector = (CssSelector)startElement;
      if (selector.getSimpleSelectors().length == 0) return;
      selector.addBefore(ampersand, selector.getSimpleSelectors()[0]);
    }
  }
}