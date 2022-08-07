package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.template.FileTypeBasedContextType;
import com.intellij.javascript.flex.FlexApplicationComponent;

public class MxmlTemplateContextType extends FileTypeBasedContextType {
  protected MxmlTemplateContextType() {
    super(FlexBundle.message("dialog.edit.template.checkbox.mxml"), FlexApplicationComponent.MXML);
  }
}
