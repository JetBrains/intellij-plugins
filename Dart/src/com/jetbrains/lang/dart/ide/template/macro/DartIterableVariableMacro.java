package com.jetbrains.lang.dart.ide.template.macro;

import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartIterableVariableMacro extends DartFilterByClassMacro {
  @Override
  public String getName() {
    return "dartIterableVariable";
  }

  @Override
  public String getPresentableName() {
    return "dartIterableVariable()";
  }

  @Override
  protected boolean filter(@NotNull DartClass dartClass) {
    return dartClass.findMemberByName("iterator") != null;
  }
}
