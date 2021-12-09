// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefileBlock extends PsiElement {

  @NotNull
  List<MakefileCommand> getCommandList();

  @NotNull
  List<MakefileConditional> getConditionalList();

  @NotNull
  List<MakefileDirective> getDirectiveList();

  @NotNull
  List<MakefileFunction> getFunctionList();

  @NotNull
  List<MakefileRule> getRuleList();

  @NotNull
  List<MakefileVariableAssignment> getVariableAssignmentList();

}
