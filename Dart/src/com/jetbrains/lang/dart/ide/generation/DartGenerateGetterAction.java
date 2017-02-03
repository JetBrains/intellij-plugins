package com.jetbrains.lang.dart.ide.generation;

import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

public class DartGenerateGetterAction extends BaseDartGenerateAction {
  @Override
  @NotNull
  protected BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateAccessorHandler(CreateGetterSetterFix.Strategy.GETTER) {
      @Override
      @NotNull
      protected String getTitle() {
        return DartBundle.message("fields.to.generate.getters");
      }
    };
  }
}
