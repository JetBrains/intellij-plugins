// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefileVariableAssignment extends PsiElement {

  @NotNull
  MakefileVariable getVariable();

  @Nullable
  MakefileVariableValue getVariableValue();

  @Nullable
  PsiElement getAssignment();

  @Nullable
  String getValue();

}
