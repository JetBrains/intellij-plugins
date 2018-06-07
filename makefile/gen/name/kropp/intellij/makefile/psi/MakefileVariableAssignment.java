// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface MakefileVariableAssignment extends PsiElement {

  @Nullable
  MakefileFunction getFunction();

  @NotNull
  MakefileVariable getVariable();

  @Nullable
  MakefileVariableValue getVariableValue();

  @Nullable
  PsiElement getAssignment();

  @Nullable
  String getValue();

}
