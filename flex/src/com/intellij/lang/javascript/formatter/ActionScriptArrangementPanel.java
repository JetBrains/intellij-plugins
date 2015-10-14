package com.intellij.lang.javascript.formatter;

import com.intellij.application.options.codeStyle.arrangement.ArrangementSettingsPanel;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class ActionScriptArrangementPanel extends ArrangementSettingsPanel {

  public ActionScriptArrangementPanel(@NotNull CodeStyleSettings settings) {
    super(settings, JavaScriptSupportLoader.ECMA_SCRIPT_L4);
  }

  @Override
  protected int getRightMargin() {
    return 80;
  }

  @NotNull
  @Override
  protected FileType getFileType() {
    return ActionScriptFileType.INSTANCE;
  }

  @Override
  protected String getPreviewText() {
    return null;
  }
}
