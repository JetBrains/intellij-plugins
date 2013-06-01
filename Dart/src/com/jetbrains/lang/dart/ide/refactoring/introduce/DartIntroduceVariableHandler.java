package com.jetbrains.lang.dart.ide.refactoring.introduce;

import com.jetbrains.lang.dart.DartBundle;

/**
 * @author: Fedor.Korotkov
 */
public class DartIntroduceVariableHandler extends DartIntroduceHandler {
  public DartIntroduceVariableHandler() {
    super(DartBundle.message("refactoring.introduce.variable.dialog.title"));
  }

  @Override
  protected String getDeclarationString(DartIntroduceOperation operation, String initExpression) {
    return "var " + operation.getName() + " = " + initExpression + ";";
  }
}
