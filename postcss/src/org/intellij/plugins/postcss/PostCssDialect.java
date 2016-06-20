package org.intellij.plugins.postcss;

import com.intellij.lang.css.CssDialect;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;

public class PostCssDialect extends CssDialect {
  @Override
  public String getName() {
    return "PostCSS";
  }

  @Override
  public String getDisplayName() {
    return "PostCSS";
  }

  @Override
  public boolean isDefault(@NotNull Module module) {
    return false;
  }
}
