package com.intellij.tapestry.intellij;

import com.intellij.lang.Language;
import com.intellij.psi.css.EmbeddedCssProvider;
import com.intellij.tapestry.lang.TmlLanguage;
import org.jetbrains.annotations.NotNull;

public class TapestryEmbeddedCssProvider extends EmbeddedCssProvider {
  @Override
  public boolean enableEmbeddedCssFor(@NotNull Language language) {
    return language.is(TmlLanguage.INSTANCE);
  }
}