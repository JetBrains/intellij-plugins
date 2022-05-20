// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.codeStyle.arrangement.ArrangementSettingsPanel;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.formatter.punctuation.JSCodeStylePunctuationPanel;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class ActionScriptCodeStyleMainPanel extends JSDerivedLanguageCodeStyleMainPanel {

  protected ActionScriptCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(JavaScriptSupportLoader.ECMA_SCRIPT_L4, currentSettings, settings);
  }

  @Override
  protected void initTabs(CodeStyleSettings settings) {
    super.initTabs(settings);
    addTab(new JSCodeStylePunctuationPanel(JavaScriptSupportLoader.ECMA_SCRIPT_L4, settings, false) {
      @NotNull
      @Override
      protected FileType getFileType() {
        return ActionScriptFileType.INSTANCE;
      }
    });
    addTab(new JSGeneratedCodeStylePanel(JavaScriptSupportLoader.ECMA_SCRIPT_L4, settings, true) {
      @NotNull
      @Override
      protected FileType getFileType() {
        return ActionScriptFileType.INSTANCE;
      }
    });
    addTab(new ArrangementSettingsPanel(settings, JavaScriptSupportLoader.ECMA_SCRIPT_L4));
  }
}
