package org.intellij.plugins.postcss.formatter;

import com.intellij.lang.Language;
import com.intellij.psi.css.impl.util.editor.CssFormattingModelBuilder;
import org.intellij.plugins.postcss.PostCssLanguage;
import org.jetbrains.annotations.NotNull;

public class PostCssFormattingModelBuilder extends CssFormattingModelBuilder {
  @Override
  protected CssFormattingExtension createExtension() {
    return new PostCssFormattingExtension();
  }

  private static class PostCssFormattingExtension extends CssFormattingExtension {
    @NotNull
    @Override
    public Language getLanguage() {
      return PostCssLanguage.INSTANCE;
    }
  }
}
