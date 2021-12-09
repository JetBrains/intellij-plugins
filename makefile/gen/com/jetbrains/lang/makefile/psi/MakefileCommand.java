// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefileCommand extends PsiElement {

  @NotNull
  List<MakefileFunction> getFunctionList();

  @NotNull
  List<MakefileString> getStringList();

  @NotNull
  List<MakefileSubstitution> getSubstitutionList();

  @NotNull
  List<MakefileVariableUsage> getVariableUsageList();

}
