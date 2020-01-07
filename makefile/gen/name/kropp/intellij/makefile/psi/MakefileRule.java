// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MakefileRule extends PsiElement {

  @Nullable
  MakefileRecipe getRecipe();

  @NotNull
  MakefileTargetLine getTargetLine();

  @NotNull
  List<MakefileTarget> getTargets();

}
