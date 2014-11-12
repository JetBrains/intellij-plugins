package com.jetbrains.lang.dart.folding;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;
import com.jetbrains.lang.dart.DartBundle;

public class DartCodeFoldingOptionsProvider extends BeanConfigurable<DartCodeFoldingSettings> implements CodeFoldingOptionsProvider {
  public DartCodeFoldingOptionsProvider() {
    super(DartCodeFoldingSettings.getInstance());
    // arguments must be equal to property in DartCodeFoldingSettings
    checkBox("CollapseParts", DartBundle.message("checkbox.collapse.parts"));
    checkBox("CollapseGenericParameters", DartBundle.message("checkbox.collapse.generic.parameters"));
  }
}
