package com.intellij.javascript.karma.scope;

import com.intellij.javascript.karma.execution.KarmaRunSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class KarmaAllScopeView extends KarmaScopeView {

  @NotNull
  @Override
  public JComponent getComponent() {
    return new JPanel();
  }

  @Override
  public void resetFrom(@NotNull KarmaRunSettings settings) {
  }

  @Override
  public void applyTo(@NotNull KarmaRunSettings.Builder builder) {
  }
}
