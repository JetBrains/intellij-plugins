package com.jetbrains.lang.dart.application.options.editor;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.folding.DartCodeFoldingSettings;

public class DartCodeFoldingOptionsProvider extends BeanConfigurable<DartCodeFoldingSettings> implements CodeFoldingOptionsProvider {
  public DartCodeFoldingOptionsProvider() {
    super(DartCodeFoldingSettings.getInstance());
    checkBox("COLLAPSE_GENERIC_PARAMETERS", DartBundle.message("checkbox.collapse.generic.parameters"));
  }
}
