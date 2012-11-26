package com.intellij.javascript.flex.codeinsight;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.flex.MxmlFileType;
import com.intellij.psi.css.EmbeddedCssProvider;
import org.jetbrains.annotations.NotNull;

/**
 * User: Andrey.Vokin
 * Date: 11/26/12
 */
public class FlexEmbeddedCssProvider extends EmbeddedCssProvider {
  @Override
  public boolean enableEmbeddedCssFor(@NotNull Language language) {
    return MxmlFileType.LANGUAGE.equals(language);
  }
}
