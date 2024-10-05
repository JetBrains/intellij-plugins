// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.codeStyle.arrangement.ArrangementSettingsPanel;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.formatter.punctuation.JSCodeStylePunctuationPanel;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class ActionScriptCodeStyleMainPanel extends JSDerivedLanguageCodeStyleMainPanel {

  protected ActionScriptCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(FlexSupportLoader.ECMA_SCRIPT_L4, currentSettings, settings);
  }

  @Override
  protected void initTabs(CodeStyleSettings settings) {
    super.initTabs(settings);
    addTab(new JSCodeStylePunctuationPanel(FlexSupportLoader.ECMA_SCRIPT_L4, settings, false) {
      @Override
      protected @NotNull FileType getFileType() {
        return ActionScriptFileType.INSTANCE;
      }
    });
    addTab(new JSGeneratedCodeStylePanel(FlexSupportLoader.ECMA_SCRIPT_L4, settings, true) {
      @Override
      protected @NotNull FileType getFileType() {
        return ActionScriptFileType.INSTANCE;
      }
    });
    addTab(new ArrangementSettingsPanel(settings, FlexSupportLoader.ECMA_SCRIPT_L4));
  }
}
