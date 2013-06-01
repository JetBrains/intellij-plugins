package com.jetbrains.lang.dart.ide.refactoring.introduce;

import com.jetbrains.lang.dart.DartBundle;

/**
 * @author: Fedor.Korotkov
 */
public class DartIntroduceFinalVariableHandler extends DartIntroduceHandler {
  public DartIntroduceFinalVariableHandler() {
    super(DartBundle.message("refactoring.introduce.final.variable.dialog.title"));
  }

  @Override
  protected String getDeclarationString(DartIntroduceOperation operation, String initExpression) {
    // todo: check if operation.getInitializer() is constant
    return "final " + operation.getName() + " = " + initExpression + ";";
  }
}
