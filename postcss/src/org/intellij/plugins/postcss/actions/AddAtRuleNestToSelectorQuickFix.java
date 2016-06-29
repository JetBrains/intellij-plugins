package org.intellij.plugins.postcss.actions;

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.css.CssSelectorList;
import org.intellij.plugins.postcss.PostCssBundle;
import org.intellij.plugins.postcss.psi.PostCssElementGenerator;
import org.intellij.plugins.postcss.psi.impl.PostCssNestSymImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AddAtRuleNestToSelectorQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
  public AddAtRuleNestToSelectorQuickFix(@NotNull CssSelectorList list) {
    super(list);
  }

  @NotNull
  @Override
  public String getText() {
    return PostCssBundle.message("annotator.add.at.rule.nest.quickfix.name");
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
    PostCssNestSymImpl nest = PostCssElementGenerator.createAtRuleNest(project);
    if (nest == null) return;
    if (startElement instanceof CssSelectorList) {
      CssSelectorList list = (CssSelectorList)startElement;
      if (list.getSelectors().length == 0) return;
      CssSelector selector = list.getSelectors()[0];
      if (selector.getSimpleSelectors().length == 0) return;
      selector.addBefore(nest, selector.getSimpleSelectors()[0]);
    }
  }
}