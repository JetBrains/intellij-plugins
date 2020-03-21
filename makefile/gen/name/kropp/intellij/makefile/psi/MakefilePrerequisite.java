// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import name.kropp.intellij.makefile.psi.impl.MakefilePrerequisiteImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MakefilePrerequisite extends PsiElement {

  @Nullable
  MakefileFunction getFunction();

  @Nullable
  MakefileIdentifier getIdentifier();

  @Nullable
  MakefileVariableUsage getVariableUsage();

  @NotNull
  MakefilePrerequisiteImpl updateText(@NotNull String newText);

  boolean isPhonyTarget();

}
