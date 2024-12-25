// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.generation;

import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

public class DartGenerateSetterAction extends BaseDartGenerateAction {
  @Override
  protected @NotNull BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateAccessorHandler(CreateGetterSetterFix.Strategy.SETTER) {
      @Override
      protected @NotNull String getTitle() {
        return DartBundle.message("fields.to.generate.setters");
      }
    };
  }
}
