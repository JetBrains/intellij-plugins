// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex;

import com.intellij.lang.javascript.dialects.JSCommonHandlersFactory;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class ActionScriptCommonHandlersFactory extends JSCommonHandlersFactory {
  @Override
  public @NotNull Class<? extends JSCodeStyleSettings> getCodeStyleSettingsClass() {
    return ECMA4CodeStyleSettings.class;
  }
}
