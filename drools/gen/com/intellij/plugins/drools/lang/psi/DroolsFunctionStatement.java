// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DroolsFunctionStatement extends DroolsFunction {

  @Nullable
  DroolsBlock getBlock();

  @NotNull
  DroolsNameId getNameId();

  @Nullable
  DroolsPrimitiveType getPrimitiveType();

  @Nullable
  DroolsType getType();

  @Nullable
  DroolsParameters getFunctionParameters();

}
