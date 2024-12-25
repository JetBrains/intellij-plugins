// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.scope;

import com.intellij.javascript.karma.execution.KarmaRunSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class KarmaScopeView {

  public abstract @NotNull JComponent getComponent();

  public abstract void resetFrom(@NotNull KarmaRunSettings settings);

  public abstract void applyTo(@NotNull KarmaRunSettings.Builder builder);
}
