package org.intellij.plugins.postcss.rename;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.util.ProcessingContext;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.jetbrains.annotations.NotNull;

public class PostCssInputValidator implements RenameInputValidator {
  @NotNull
  @Override
  public ElementPattern<? extends PsiElement> getPattern() {
    return PlatformPatterns.psiElement(PostCssCustomSelector.class);
  }

  public boolean isInputValid(@NotNull final String newName, @NotNull final PsiElement element, @NotNull final ProcessingContext context) {
    if (element instanceof PostCssCustomSelector) {
      return StringUtil.startsWith(newName, "--");
    }
    throw new IllegalArgumentException("Unexpected element: " + element);
  }
}