// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MakefileVariableAssignment extends PsiElement {

  @NotNull
  MakefileVariable getVariable();

  @Nullable
  MakefileVariableValue getVariableValue();

}
