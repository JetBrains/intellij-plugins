package com.jetbrains.lang.dart.ide.generation;

import com.jetbrains.lang.dart.DartBundle;

/**
 * @author: Fedor.Korotkov
 */
public class DartGenerateGetterAction extends BaseDartGenerateAction {
  @Override
  protected BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateAccessorHandler(CreateGetterSetterFix.Strategy.GETTER) {
      @Override
      protected String getTitle() {
        return DartBundle.message("fields.to.generate.getters");
      }
    };
  }
}
