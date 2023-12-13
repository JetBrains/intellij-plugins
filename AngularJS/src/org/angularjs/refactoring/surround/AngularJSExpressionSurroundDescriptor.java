package org.angularjs.refactoring.surround;

import com.intellij.lang.javascript.surroundWith.JSExpressionSurroundDescriptor;
import com.intellij.lang.javascript.surroundWith.JSNotWithParenthesesSurrounder;
import com.intellij.lang.javascript.surroundWith.JSWithParenthesesSurrounder;
import com.intellij.lang.surroundWith.Surrounder;
import org.jetbrains.annotations.NotNull;


public final class AngularJSExpressionSurroundDescriptor extends JSExpressionSurroundDescriptor {

  private static final Surrounder[] SURROUNDERS = {
    new JSWithParenthesesSurrounder(),
    new JSNotWithParenthesesSurrounder()
  };

  @Override
  public Surrounder @NotNull [] getSurrounders() {
    return SURROUNDERS;
  }
}
