package com.jetbrains.lang.dart.ide.generation;

import com.jetbrains.lang.dart.DartBundle;

/**
 * @author: Fedor.Korotkov
 */
public class DartGenerateSetterAction extends BaseDartGenerateAction {
  @Override
  protected BaseDartGenerateHandler getGenerateHandler() {
    return new DartGenerateAccessorHandler(CreateGetterSetterFix.Strategy.SETTER) {
      @Override
      protected String getTitle() {
        return DartBundle.message("fields.to.generate.setters");
      }
    };
  }
}
