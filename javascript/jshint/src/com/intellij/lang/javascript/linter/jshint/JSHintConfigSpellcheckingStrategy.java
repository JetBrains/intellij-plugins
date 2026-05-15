package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.jshint.config.JSHintConfigFileUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public class JSHintConfigSpellcheckingStrategy extends SpellcheckingStrategy implements DumbAware {
  @Override
  public @NotNull Tokenizer getTokenizer(PsiElement element) {
    return EMPTY_TOKENIZER;
  }

  @Override
  public boolean isMyContext(@NotNull PsiElement element) {
    return JSHintConfigFileUtil.isJSHintConfigFile(element);
  }
}
