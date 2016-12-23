// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface MakefileRecipe extends PsiElement {

  @Nullable
  MakefileCommands getCommands();

  @Nullable
  MakefileConditional getConditional();

}
