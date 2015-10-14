package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.template.FileTypeBasedContextType;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: maxim.mossienko
 * Date: 28.04.11
 * Time: 16:20
 */
public class MxmlTemplateContextType extends FileTypeBasedContextType {
  protected MxmlTemplateContextType() {
    super("MXML", FlexBundle.message("dialog.edit.template.checkbox.mxml"), FlexApplicationComponent.MXML);
  }
}
