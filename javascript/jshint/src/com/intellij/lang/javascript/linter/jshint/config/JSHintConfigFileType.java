package com.intellij.lang.javascript.linter.jshint.config;

import com.intellij.javascript.common.icons.JavascriptCommonIcons;
import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

/**
 * @author Sergey Simonchik
 */
public final class JSHintConfigFileType extends JsonFileType {

  public static final JSHintConfigFileType INSTANCE = new JSHintConfigFileType();

  private JSHintConfigFileType() {
    super(JsonLanguage.INSTANCE, true);
  }

  @Override
  public @NotNull String getName() {
    return "JSHint";
  }

  @Override
  public @NotNull String getDescription() {
    return JSHintBundle.message("filetype.jshint.config.description");
  }
  @Override
  public @NotNull String getDisplayName() {
    return JSHintBundle.message("filetype.jshint.config.display.name");
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "jshintrc";
  }

  @Override
  public Icon getIcon() {
    return JavascriptCommonIcons.FileTypes.JsHint;
  }

}
