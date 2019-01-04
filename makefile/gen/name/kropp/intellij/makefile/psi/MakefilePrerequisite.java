// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import name.kropp.intellij.makefile.psi.impl.MakefilePrerequisiteImpl;
import org.jetbrains.annotations.Nullable;

public interface MakefilePrerequisite extends PsiElement {

  @Nullable
  MakefileFunction getFunction();

  MakefilePrerequisiteImpl updateText(String newText);

  boolean isPhonyTarget();

}
