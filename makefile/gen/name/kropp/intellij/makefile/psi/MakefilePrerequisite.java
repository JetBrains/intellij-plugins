// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import name.kropp.intellij.makefile.psi.impl.MakefilePrerequisiteImpl;

public interface MakefilePrerequisite extends PsiElement {

  @Nullable
  MakefileFunction getFunction();

  @NotNull
  MakefilePrerequisiteImpl updateText(@NotNull String newText);

  boolean isPhonyTarget();

}
