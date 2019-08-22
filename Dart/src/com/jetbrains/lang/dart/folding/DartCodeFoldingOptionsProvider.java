package com.jetbrains.lang.dart.folding;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;
import com.jetbrains.lang.dart.DartBundle;

public class DartCodeFoldingOptionsProvider extends BeanConfigurable<DartCodeFoldingSettings> implements CodeFoldingOptionsProvider {
  public DartCodeFoldingOptionsProvider() {
    super(DartCodeFoldingSettings.getInstance(), "Dart");
    DartCodeFoldingSettings settings = getInstance();
    checkBox(DartBundle.message("checkbox.collapse.parts"), settings::isCollapseParts, settings::setCollapseParts);
    checkBox(DartBundle.message("checkbox.collapse.generic.parameters"), settings::isCollapseGenericParameters, settings::setCollapseGenericParameters);
  }
}
