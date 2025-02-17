package com.jetbrains.plugins.jade.psi;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.jetbrains.plugins.jade.JadeBundle;
import com.jetbrains.plugins.jade.JadeIcons;
import com.jetbrains.plugins.jade.JadeLanguage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class JadeFileType extends LanguageFileType {

  public static final JadeFileType INSTANCE = new JadeFileType();

  private JadeFileType() {
    super(JadeLanguage.INSTANCE);
  }

  @Override
  public @NotNull String getName() {
    return "Jade";
  }

  @Override
  public @NotNull String getDescription() {
    return JadeBundle.message("filetype.jade.description");
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "jade";
  }

  @Override
  public Icon getIcon() {
    return JadeIcons.Pug;
  }
}
