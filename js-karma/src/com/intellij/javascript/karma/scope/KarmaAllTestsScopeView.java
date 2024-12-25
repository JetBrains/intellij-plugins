// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.scope;

import com.intellij.javascript.karma.execution.KarmaRunSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class KarmaAllTestsScopeView extends KarmaScopeView {

  @Override
  public @NotNull JComponent getComponent() {
    return new JPanel();
  }

  @Override
  public void resetFrom(@NotNull KarmaRunSettings settings) {
  }

  @Override
  public void applyTo(@NotNull KarmaRunSettings.Builder builder) {
  }
}
