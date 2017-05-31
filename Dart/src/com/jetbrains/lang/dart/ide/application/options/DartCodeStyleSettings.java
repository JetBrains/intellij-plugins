package com.jetbrains.lang.dart.ide.application.options;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class DartCodeStyleSettings extends CustomCodeStyleSettings {

  public boolean DELEGATE_TO_DARTFMT = false;

  public DartCodeStyleSettings(@NotNull final CodeStyleSettings container) {
    super("DartCodeStyleSettings", container);
  }
}
