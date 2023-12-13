package com.jetbrains.lang.dart.ide.template.macro;

import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;

public final class DartListVariableMacro extends DartFilterByClassMacro {
  @Override
  public String getName() {
    return "dartListVariable";
  }

  @Override
  protected boolean filter(@NotNull DartClass dartClass) {
    return dartClass.findMemberByName("length") != null && dartClass.findOperator("[]", null) != null;
  }
}
