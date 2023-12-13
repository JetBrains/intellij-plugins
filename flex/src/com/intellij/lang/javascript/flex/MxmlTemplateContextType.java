package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.template.FileTypeBasedContextType;
import com.intellij.javascript.flex.FlexApplicationComponent;

public final class MxmlTemplateContextType extends FileTypeBasedContextType {
  private MxmlTemplateContextType() {
    super(FlexBundle.message("dialog.edit.template.checkbox.mxml"), FlexApplicationComponent.MXML);
  }
}
