// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MakefileBlock extends PsiElement {

  @NotNull
  List<MakefileCommand> getCommandList();

  @NotNull
  List<MakefileConditional> getConditionalList();

  @NotNull
  List<MakefileDirective> getDirectiveList();

  @Nullable
  MakefileEmptyCommand getEmptyCommand();

  @NotNull
  List<MakefileFunction> getFunctionList();

  @NotNull
  List<MakefileRule> getRuleList();

  @NotNull
  List<MakefileVariableAssignment> getVariableAssignmentList();

}
