package org.intellij.plugins.postcss.settings;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.css.codeStyle.CssCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class PostCssCodeStyleSettings extends CssCodeStyleSettings {
  public boolean COMMENTS_INLINE_STYLE = true;

  protected PostCssCodeStyleSettings(@NotNull CodeStyleSettings settings) {
    super("PostCssCodeStyleSettings", settings);
  }
}
