package com.intellij.plugins.drools.lang.lexer;

import com.intellij.plugins.drools.DroolsLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DroolsElementType extends IElementType {

  public DroolsElementType(@NotNull @NonNls String debugName) {
    super(debugName, DroolsLanguage.INSTANCE);
  }
}
