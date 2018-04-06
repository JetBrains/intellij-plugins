package com.intellij.lang.javascript.linter.tslint.ide;

import com.intellij.icons.AllIcons;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Irina.Chernushina on 8/31/2015.
 */
public class TsLintConfigFileType extends LanguageFileType {
  public static final TsLintConfigFileType INSTANCE = new TsLintConfigFileType();


  protected TsLintConfigFileType() {
    super(JsonLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "TSLint";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "TSLint configuration";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "json";
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return AllIcons.FileTypes.Json;
  }
}
