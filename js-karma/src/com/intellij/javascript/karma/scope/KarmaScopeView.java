package com.intellij.javascript.karma.scope;

import com.intellij.javascript.karma.execution.KarmaRunSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class KarmaScopeView {

  @NotNull
  public abstract JComponent getComponent();

  public abstract void resetFrom(@NotNull KarmaRunSettings settings);

  public abstract void applyTo(@NotNull KarmaRunSettings.Builder builder);
}
