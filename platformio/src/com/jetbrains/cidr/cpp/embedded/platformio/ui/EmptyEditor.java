package com.jetbrains.cidr.cpp.embedded.platformio.ui;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class EmptyEditor<T> extends SettingsEditor<T> {
  @Override
  protected void resetEditorFrom(@NotNull T cMakeAppRunConfiguration) {

  }

  @Override
  protected void applyEditorTo(@NotNull T cMakeAppRunConfiguration) {

  }

  @Override
  protected @NotNull
  JComponent createEditor() {
    return new JBPanel<>();
  }
}
