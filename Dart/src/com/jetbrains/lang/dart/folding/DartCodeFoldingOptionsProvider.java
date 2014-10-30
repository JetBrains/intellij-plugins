package com.jetbrains.lang.dart.folding;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;
import com.jetbrains.lang.dart.DartBundle;

public class DartCodeFoldingOptionsProvider extends BeanConfigurable<DartCodeFoldingSettings> implements CodeFoldingOptionsProvider {
  public DartCodeFoldingOptionsProvider() {
    super(DartCodeFoldingSettings.getInstance());
    // argument must be equal to property in DartCodeFoldingSettings
    checkBox("CollapseGenericParameters", DartBundle.message("checkbox.collapse.generic.parameters"));
  }
}
