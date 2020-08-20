// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.generation;

import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

public class DartGenerateGetterSetterAction extends BaseDartGenerateAction {
  @Override
  @NotNull
  protected BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateAccessorHandler(CreateGetterSetterFix.Strategy.GETTER_SETTER) {
      @Override
      @NotNull
      protected String getTitle() {
        return DartBundle.message("fields.to.generate.getters.setters");
      }
    };
  }
}
