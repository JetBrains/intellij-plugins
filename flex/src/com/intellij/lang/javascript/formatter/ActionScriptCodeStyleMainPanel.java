package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.formatter.punctuation.JSCodeStylePunctuationPanel;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

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
    addTab(new JSCodeStylePunctuationPanel(JavaScriptSupportLoader.ECMA_SCRIPT_L4, settings, false) {
      @NotNull
      @Override
      protected FileType getFileType() {
        return ActionScriptFileType.INSTANCE;
      }
    });
    addTab(new JSGeneratedCodeStylePanel(JavaScriptSupportLoader.ECMA_SCRIPT_L4, settings) {
      @NotNull
      @Override
      protected FileType getFileType() {
        return ActionScriptFileType.INSTANCE;
      }
    });
    addTab(new ActionScriptArrangementPanel(settings));
  }
}
