package com.jetbrains.plugins.jade;

import com.intellij.lang.Language;
import com.intellij.psi.css.EmbeddedCssProvider;
import org.jetbrains.annotations.NotNull;

public final class JadeEmbeddedCssProvider extends EmbeddedCssProvider {
  @Override
  public boolean enableEmbeddedCssFor(@NotNull Language language) {
    return JadeLanguage.INSTANCE.equals(language);
  }

}
