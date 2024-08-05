// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex;

import com.intellij.javascript.flex.completion.ActionScriptCompletionKeywordsContributor;
import com.intellij.lang.javascript.completion.JSCompletionKeywordsContributor;
import com.intellij.lang.javascript.dialects.JSHandlersFactory;
import com.intellij.lang.javascript.formatter.ECMA4CodeStyleSettings;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin.Ulitin
 */
public final class ActionScriptHandlersFactory extends JSHandlersFactory {

  @Override
  public @NotNull JSCompletionKeywordsContributor newCompletionKeywordsContributor() {
    return new ActionScriptCompletionKeywordsContributor();
  }

  @Override
  public @NotNull Class<? extends JSCodeStyleSettings> getCodeStyleSettingsClass() {
    return ECMA4CodeStyleSettings.class;
  }
}
