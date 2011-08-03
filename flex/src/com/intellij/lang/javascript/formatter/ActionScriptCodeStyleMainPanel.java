package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.MultiTabCodeStyleAbstractPanel;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.psi.codeStyle.CodeStyleSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rustam Vishnyakov
 */
public class ActionScriptCodeStyleMainPanel extends MultiTabCodeStyleAbstractPanel {

  protected ActionScriptCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
    super(currentSettings, settings);
  }


  @Override
  protected void initTabs(CodeStyleSettings settings) {
    super.initTabs(settings);
    addTab(new ActionScriptCodeStylePanel(settings));
  }

  @Override
  public Language getDefaultLanguage() {
    return JavaScriptSupportLoader.ECMA_SCRIPT_L4;
  }
}
