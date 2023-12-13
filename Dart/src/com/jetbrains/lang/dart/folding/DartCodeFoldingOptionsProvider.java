// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.folding;

import com.intellij.application.options.editor.CodeFoldingOptionsProvider;
import com.intellij.openapi.options.BeanConfigurable;
import com.jetbrains.lang.dart.DartBundle;

public final class DartCodeFoldingOptionsProvider extends BeanConfigurable<DartCodeFoldingSettings> implements CodeFoldingOptionsProvider {
  public DartCodeFoldingOptionsProvider() {
    super(DartCodeFoldingSettings.getInstance(), DartBundle.message("dart.title"));
    DartCodeFoldingSettings settings = getInstance();
    checkBox(DartBundle.message("checkbox.collapse.parts"), settings::isCollapseParts, settings::setCollapseParts);
    checkBox(DartBundle.message("checkbox.collapse.generic.parameters"), settings::isCollapseGenericParameters,
             settings::setCollapseGenericParameters);
  }
}
