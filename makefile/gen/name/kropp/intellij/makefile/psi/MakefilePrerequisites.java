// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MakefilePrerequisites extends PsiElement {

  @NotNull
  MakefileNormalPrerequisites getNormalPrerequisites();

  @Nullable
  MakefileOrderOnlyPrerequisites getOrderOnlyPrerequisites();

}
