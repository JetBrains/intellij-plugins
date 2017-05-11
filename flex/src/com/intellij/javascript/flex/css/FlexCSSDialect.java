package com.intellij.javascript.flex.css;


import com.intellij.lang.css.CssDialect;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;

public class FlexCSSDialect extends CssDialect {
  @Override
  public String getName() {
    return "FLEX";
  }

  @Override
  public String getDisplayName() {
    return FlexBundle.message("css.flex.dialect.name");
  }

  @Override
  public boolean isDefault(@NotNull Module module) {
    return ModuleType.get(module) == FlexModuleType.getInstance();
  }

  public static CssDialect getInstance() {
    return CssDialect.EP_NAME.findExtension(FlexCSSDialect.class);
  }
}
