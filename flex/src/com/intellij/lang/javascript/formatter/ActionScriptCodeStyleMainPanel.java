package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.psi.codeStyle.CodeStyleSettings;

/**
 * @author Rustam Vishnyakov
 */
public class ActionScriptCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {

  protected ActionScriptCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(JavaScriptSupportLoader.ECMA_SCRIPT_L4, currentSettings, settings);
  }


  @Override
  protected void initTabs(CodeStyleSettings settings) {
    super.initTabs(settings);
    addTab(new ActionScriptCodeStylePanel(settings));
  }

}
