package com.jetbrains.lang.dart.ide.template.macro;

import com.intellij.codeInsight.template.Macro;
import com.intellij.codeInsight.template.TemplateContextType;
import com.jetbrains.lang.dart.ide.template.DartTemplateContextType;

public abstract class DartMacroBase extends Macro {
  @Override
  public final boolean isAcceptableInContext(final TemplateContextType context) {
    return context instanceof DartTemplateContextType;
  }
}
