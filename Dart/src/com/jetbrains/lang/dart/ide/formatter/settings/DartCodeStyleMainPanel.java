// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.formatter.settings;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.ide.application.options.DartfmtCodeStylePanel;
import org.jetbrains.annotations.NotNull;

public class DartCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
  protected DartCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(DartLanguage.INSTANCE, currentSettings, settings);
  }

  @Override
  protected void initTabs(@NotNull final CodeStyleSettings settings) {
    addTab(new DartfmtCodeStylePanel(settings));
    super.initTabs(settings);
  }
}
